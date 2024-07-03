package com.matthewperiut.fence_on_slab.mixin;

import net.minecraft.block.*;
import net.minecraft.block.enums.SlabType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.matthewperiut.fence_on_slab.FenceOnSlab.FENCE_SLAB_SUPPORT;
import static com.matthewperiut.fence_on_slab.FenceOnSlab.LOWER;

@Mixin(FenceBlock.class)
abstract public class FenceBlockMixin extends HorizontalConnectingBlock {
    @Shadow public abstract boolean canConnect(BlockState state, boolean neighborIsFullSquare, Direction dir);

    @Unique
    protected VoxelShape[] lowerCollisionShapes = null;
    @Unique
    protected VoxelShape[] lowerBoundingShapes = null;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(Settings settings, CallbackInfo ci) {
        lowerCollisionShapes = new VoxelShape[collisionShapes.length];
        lowerBoundingShapes = new VoxelShape[boundingShapes.length];
        for (int i = 0; i < collisionShapes.length; i++) {
            lowerCollisionShapes[i] = collisionShapes[i].offset(0, -0.5, 0);
        }
        for (int i = 0; i < boundingShapes.length; i++) {
            lowerBoundingShapes[i] = boundingShapes[i].offset(0, -0.5, 0);
        }

        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(NORTH, false)).with(EAST, false)).with(SOUTH, false)).with(WEST, false)).with(WATERLOGGED, false).with(LOWER, false));
    }


    protected FenceBlockMixin(float radius1, float radius2, float boundingHeight1, float boundingHeight2, float collisionHeight, Settings settings) {
        super(radius1, radius2, boundingHeight1, boundingHeight2, collisionHeight, settings);
    }

    @Unique
    private boolean sameHalf(boolean lower, BlockState blockState) {
        if (blockState.getBlock() instanceof FenceBlock)
            return blockState.get(LOWER) == lower;
        else
            return true;
    }

    @Unique
    private boolean isLower(BlockState blockState) {
        if (blockState.getBlock() instanceof SlabBlock)
            return blockState.get(SlabBlock.TYPE).equals(SlabType.BOTTOM);
        if (blockState.getBlock() instanceof FenceBlock)
            return blockState.get(LOWER);
        else
            return false;
    }

    @Inject(method = "getPlacementState", at = @At("HEAD"), cancellable = true)
    private void onGetPlacementState(ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> cir) {
        try {
            if (!FENCE_SLAB_SUPPORT.contains(getTranslationKey().split("\\.")[2]))
                return;
        } catch (Exception e) {
            System.out.println("Report to Fence On Slab Github Issues");
            System.out.println(e.getMessage());
            return;
        }

        BlockView blockView = ctx.getWorld();
        BlockPos blockPos = ctx.getBlockPos();
        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
        BlockPos blockPos2 = blockPos.north();
        BlockPos blockPos3 = blockPos.east();
        BlockPos blockPos4 = blockPos.south();
        BlockPos blockPos5 = blockPos.west();
        BlockState blockState = blockView.getBlockState(blockPos2);
        BlockState blockState2 = blockView.getBlockState(blockPos3);
        BlockState blockState3 = blockView.getBlockState(blockPos4);
        BlockState blockState4 = blockView.getBlockState(blockPos5);

        boolean lower = isLower(blockView.getBlockState(blockPos.down()));

        BlockState state = super.getPlacementState(ctx)
                .with(NORTH, sameHalf(lower, blockState) && canConnect(blockState, blockState.isSideSolidFullSquare(blockView, blockPos2, Direction.SOUTH), Direction.SOUTH))
                .with(EAST, sameHalf(lower, blockState2) && canConnect(blockState2, blockState2.isSideSolidFullSquare(blockView, blockPos3, Direction.WEST), Direction.WEST))
                .with(SOUTH, sameHalf(lower, blockState3) && canConnect(blockState3, blockState3.isSideSolidFullSquare(blockView, blockPos4, Direction.NORTH), Direction.NORTH))
                .with(WEST, sameHalf(lower, blockState4) && canConnect(blockState4, blockState4.isSideSolidFullSquare(blockView, blockPos5, Direction.EAST), Direction.EAST))
                .with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER)
                .with(LOWER, lower);
        cir.setReturnValue(state);
    }

    @Inject(method = "getStateForNeighborUpdate", at = @At("HEAD"), cancellable = true)
    protected void onStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos, CallbackInfoReturnable<BlockState> cir) {
        try {
            if (!FENCE_SLAB_SUPPORT.contains(getTranslationKey().split("\\.")[2]))
                return;
        } catch (Exception e) {
            System.out.println("Report to Fence On Slab Github Issues");
            System.out.println(e.getMessage());
            return;
        }

        if ((Boolean)state.get(WATERLOGGED)) {
            // 1.19 - 1.21
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
            // 1.18
            //world.createAndScheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
            // 1.16.5
            //world.getFluidTickScheduler().schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        boolean lower = state.get(LOWER);
        if (direction.equals(Direction.DOWN)) {
            lower = isLower(neighborState);
        }

        BlockState blockState = direction.getAxis().getType() == Direction.Type.HORIZONTAL ? (BlockState) state.with((Property) FACING_PROPERTIES.get(direction), sameHalf(lower, neighborState) && this.canConnect(neighborState, neighborState.isSideSolidFullSquare(world, neighborPos, direction.getOpposite()), direction.getOpposite())) : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
        blockState.with(LOWER, lower);
        cir.setReturnValue(blockState);
    }

    @Inject(method = "appendProperties", at = @At("TAIL"))
    private void injectExtraProperty(StateManager.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(LOWER);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (state.get(LOWER))
            return this.lowerCollisionShapes[this.getShapeIndex(state)];
        else
            return super.getCollisionShape(state, world, pos, context);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (state.get(LOWER))
            return this.lowerBoundingShapes[this.getShapeIndex(state)];
        else
            return super.getOutlineShape(state, world, pos, context);
    }
}