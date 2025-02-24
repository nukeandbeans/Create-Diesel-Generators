package com.jesz.createdieselgenerators.blocks;

import com.jesz.createdieselgenerators.blocks.entity.BasinLidBlockEntity;
import com.jesz.createdieselgenerators.blocks.entity.BlockEntityRegistry;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BasinLidBlock extends Block implements ProperWaterloggedBlock, IBE<BasinLidBlockEntity>, IWrenchable {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ON_A_BASIN = BooleanProperty.create("on_a_basin");
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public BasinLidBlock(Properties properties) {
        super(properties);
        registerDefaultState(super.defaultBlockState().setValue(ON_A_BASIN, false));
        registerDefaultState(super.defaultBlockState().setValue(WATERLOGGED, false));
        registerDefaultState(super.defaultBlockState().setValue(OPEN, false));
        registerDefaultState(super.defaultBlockState().setValue(POWERED, false));
    }
    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        if(!pState.getValue(OPEN))
            return Shapes.or(Block.box(0,0,0,16,2,16),
                    Block.box(5,2,5,11,4,11));
        if(pState.getValue(FACING) == Direction.SOUTH)
            return Shapes.or(Block.box(0, 0, 14, 16, 16, 16),
                    Block.box(5, 5, 16, 11, 11, 18));
        if(pState.getValue(FACING) == Direction.WEST)
            return Shapes.or(Block.box(0, 0, 0, 2, 16, 16),
                    Block.box(-2, 5, 5, 0, 11, 11));
        if(pState.getValue(FACING) == Direction.NORTH)
            return Shapes.or(Block.box(0, 0, 0, 16, 16, 2),
                    Block.box(5, 5, -2, 11, 11, 0));
        return Shapes.or(Block.box(14, 0, 0, 16, 16, 16),
                Block.box(16, 5, 5, 18, 11, 11));

    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState p_60569_, boolean p_60570_) {
        super.onPlace(state, level, pos, p_60569_, p_60570_);

        if(level.getBlockEntity(pos.below()) instanceof BasinBlockEntity)
            level.setBlock(pos, state.setValue(ON_A_BASIN, true), 2);
        else
            level.setBlock(pos, state.setValue(ON_A_BASIN, false), 2);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos p_57551_, boolean p_57552_) {
        boolean flag = level.hasNeighborSignal(pos);
        if(level.getBlockEntity(pos.below()) instanceof BasinBlockEntity)
            state = state.setValue(ON_A_BASIN, true);
        else
            state = state.setValue(ON_A_BASIN, false);
        if (flag != state.getValue(POWERED)) {
            if(flag != state.getValue(OPEN))
                level.playSound(null, pos, flag ?  SoundEvents.IRON_TRAPDOOR_OPEN : SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1f, 1f);
            level.setBlock(pos, state.setValue(POWERED, flag).setValue(OPEN, flag), 2);
        }else {
            level.setBlock(pos, state, 2);
        }

    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        boolean currentState = state.getValue(OPEN);
        if(!level.isClientSide() && hand == InteractionHand.MAIN_HAND) {
            level.setBlock(pos, state.setValue(OPEN, !currentState), 3);
            level.playSound(null, pos, !currentState ?  SoundEvents.IRON_TRAPDOOR_OPEN : SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1f, 1f);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ON_A_BASIN);
        builder.add(FACING);
        builder.add(OPEN);
        builder.add(WATERLOGGED);
        builder.add(POWERED);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        if (pContext.getPlayer().isShiftKeyDown()) {
            return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
        } else {
            return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection());
        }
    }

    @Override
    public FluidState getFluidState(BlockState pState) {
        return fluidState(pState);
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState,
                                  LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pNeighborPos) {
        updateWater(pLevel, pState, pCurrentPos);
        return pState;
    }


    @Override
    public Class<BasinLidBlockEntity> getBlockEntityClass() {
        return BasinLidBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends BasinLidBlockEntity> getBlockEntityType() {
        return BlockEntityRegistry.BASIN_LID.get();
    }
}
