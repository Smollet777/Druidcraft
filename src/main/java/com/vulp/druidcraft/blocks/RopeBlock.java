package com.vulp.druidcraft.blocks;

import com.google.common.collect.Maps;
import com.vulp.druidcraft.registry.BlockRegistry;
import net.minecraft.block.*;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.ConnectionType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Map;

@SuppressWarnings("deprecation")
public class RopeBlock extends SixWayBlock implements IWaterLoggable {
    public static final BooleanProperty KNOT = BooleanProperty.create("knot");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public RopeBlock(Properties properties) {
        super(0.0625F, properties);
        this.setDefaultState(this.getDefaultState()
                .with(NORTH, false)
                .with(EAST, false)
                .with(SOUTH, false)
                .with(WEST, false)
                .with(UP, false)
                .with(DOWN, false));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, KNOT, WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext ctx) {
        return getState(getDefaultState(), ctx.getWorld(), ctx.getPos());
    }

    private BlockState calculateKnot (BlockState currentState) {
      int count = 0;

      for (Direction dir : Direction.values()) {
        BooleanProperty prop = FACING_TO_PROPERTY_MAP.get(dir);
        if (prop != null) {
          if (currentState.get(prop)) {
            count++;
          }
        }
      }

      return currentState.with(KNOT, count >= 3);
    }

  @Override
  public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
      return getState(stateIn, worldIn, facingPos);
  }

  private BlockState getState(BlockState currentState, IWorld world, BlockPos pos) {
        BlockState northState = world.getBlockState(pos.offset(Direction.NORTH));
        boolean north = northState.func_224755_d(world, pos.offset(Direction.NORTH), Direction.NORTH.getOpposite()) || northState.getBlock() == this;

        BlockState eastState = world.getBlockState(pos.offset(Direction.EAST));
        boolean east = eastState.func_224755_d(world, pos.offset(Direction.EAST), Direction.EAST.getOpposite()) || eastState.getBlock() == this;

        BlockState southState = world.getBlockState(pos.offset(Direction.SOUTH));
        boolean south = southState.func_224755_d(world, pos.offset(Direction.SOUTH), Direction.SOUTH.getOpposite()) || southState.getBlock() == this;

        BlockState westState = world.getBlockState(pos.offset(Direction.WEST));
        boolean west = westState.func_224755_d(world, pos.offset(Direction.WEST), Direction.WEST.getOpposite()) || westState.getBlock() == this;

        BlockState upState = world.getBlockState(pos.offset(Direction.UP));
        boolean up = upState.func_224755_d(world, pos.offset(Direction.UP), Direction.UP.getOpposite()) || upState.getBlock() == this;

        BlockState downState = world.getBlockState(pos.offset(Direction.DOWN));
        boolean down = downState.func_224755_d(world, pos.offset(Direction.DOWN), Direction.DOWN.getOpposite()) || downState.getBlock() == this;

        return calculateKnot(currentState
                .with(NORTH, north)
                .with(EAST, east)
                .with(SOUTH, south)
                .with(WEST, west)
                .with(UP, up)
                .with(DOWN, down));
    }
}