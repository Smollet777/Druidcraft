package com.vulp.druidcraft.blocks;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.sun.scenario.effect.Crop;
import com.vulp.druidcraft.api.CropLifeStageType;
import com.vulp.druidcraft.registry.BlockRegistry;
import com.vulp.druidcraft.registry.ItemRegistry;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.RavagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.*;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.world.storage.WorldSummary;
import net.minecraftforge.server.permission.context.WorldContext;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Random;


public class ElderFruitBlock extends CropBlock implements IGrowable {

    public static final DirectionProperty FACING = DirectionalBlock.FACING;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_0_3;
    public static final BooleanProperty MID_BERRY = BooleanProperty.create("mid_berry");

    public static final EnumProperty<CropLifeStageType> LIFE_STAGE = EnumProperty.create("life_stage", CropLifeStageType.class);

    public ElderFruitBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(this.getAgeProperty(), 0).with(LIFE_STAGE, CropLifeStageType.FLOWER).with(MID_BERRY, false).with(FACING, Direction.NORTH));
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        VoxelShape voxelshape = VoxelShapes.empty();
        Vec3d vec3d = state.getOffset(worldIn, pos);

        if (state.get(FACING) == Direction.UP) {
            voxelshape = VoxelShapes.or(voxelshape, Block.makeCuboidShape(2.0D, 15.0D, 2.0D, 14.0D, 16.0D, 14.0D));
        }
        if (state.get(FACING) == Direction.DOWN) {
            voxelshape = VoxelShapes.or(voxelshape, Block.makeCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 1.0D, 14.0D));
        }
        if (state.get(FACING) == Direction.NORTH) {
            voxelshape = VoxelShapes.or(voxelshape, Block.makeCuboidShape(2.0D, 2.0D, 0.0D, 14.0D, 14.0D, 1.0D));
        }
        if (state.get(FACING) == Direction.EAST) {
            voxelshape = VoxelShapes.or(voxelshape, Block.makeCuboidShape(15.0D, 2.0D, 2.0D, 16.0D, 14.0D, 14.0D));
        }
        if (state.get(FACING) == Direction.SOUTH) {
            voxelshape = VoxelShapes.or(voxelshape, Block.makeCuboidShape(2.0D, 2.0D, 15.0D, 14.0D, 14.0D, 16.0D));
        }
        if (state.get(FACING) == Direction.WEST) {
            voxelshape = VoxelShapes.or(voxelshape, Block.makeCuboidShape(0.0D, 2.0D, 2.0D, 1.0D, 14.0D, 14.0D));
        }
        return voxelshape.withOffset(vec3d.x, vec3d.y, vec3d.z);
    }

    @Override
    public Vec3d getOffset(BlockState state, IBlockReader worldIn, BlockPos pos) {
        long i = MathHelper.getCoordinateRandom(pos.getX(), pos.getY(), pos.getZ());
        return new Vec3d(
                !(state.get(FACING) == Direction.EAST || state.get(FACING) == Direction.WEST) ?(((i & 15L) / 15.0F) - 0.5D) * 0.5D : 0.0D,
                !(state.get(FACING) == Direction.UP || state.get(FACING) == Direction.DOWN) ? (((i >> 4 & 15L) / 15.0F) - 0.5D) * 0.5D : 0.0D,
                !(state.get(FACING) == Direction.NORTH || state.get(FACING) == Direction.SOUTH) ? (((i >> 8 & 15L) / 15.0F) - 0.5D) * 0.5D : 0.0D);
    }

    @Override
    public ItemStack getItem(IBlockReader worldIn, BlockPos pos, BlockState state) {
        return new ItemStack(this);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        for (Direction direction : context.getNearestLookingDirections()) {
            BlockState blockstate = this.getDefaultState().with(FACING, Direction.NORTH);;
            if (direction == Direction.UP) {
                blockstate = this.getDefaultState().with(FACING, Direction.UP);
            } if (direction == Direction.DOWN) {
                blockstate = this.getDefaultState().with(FACING, Direction.DOWN);
            } if (direction == Direction.NORTH) {
                blockstate = this.getDefaultState().with(FACING, Direction.NORTH);
            } if (direction == Direction.SOUTH) {
                blockstate = this.getDefaultState().with(FACING, Direction.SOUTH);
            } if (direction == Direction.EAST) {
                blockstate = this.getDefaultState().with(FACING, Direction.EAST);
            } if (direction == Direction.WEST) {
                blockstate = this.getDefaultState().with(FACING, Direction.WEST);
            }

            if (context.getWorld().getBlockState(context.getPos()).getBlock() instanceof ElderLeavesBlock) {
                return blockstate;
            }
        }
        return null;
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBlockHarvested(worldIn, pos, state, player);
        if (state.get(AGE) == getMaxAge()) {
            if (state.get(LIFE_STAGE) == CropLifeStageType.FLOWER) {
                spawnAsEntity(worldIn, pos, new ItemStack(ItemRegistry.elderflower, 1 + worldIn.rand.nextInt(1)));
            } else if (state.get(LIFE_STAGE) == CropLifeStageType.BERRY && !state.get(MID_BERRY)) {
                spawnAsEntity(worldIn, pos, new ItemStack(ItemRegistry.elderberries, 1 + worldIn.rand.nextInt(2)));
            }
        }
    }

    @Override
    public IntegerProperty getAgeProperty() {
        return AGE;
    }

    @Override
    public int getMaxAge() {
        return 3;
    }

    @Override
    protected int getAge(BlockState state) {
        return state.get(this.getAgeProperty());
    }

    @Override
    public boolean isMaxAge(BlockState state) {
        return state.get(this.getAgeProperty()) >= this.getMaxAge();
    }

    public boolean isGrowable(World worldIn, BlockPos pos) {
        if (worldIn.getBlockState(pos).getBlock() == this) {
            return worldIn.getBlockState(pos).get(LIFE_STAGE) == CropLifeStageType.FLOWER || worldIn.getBlockState(pos).get(MID_BERRY);
        }
        else return false;
    }

    @Override
    public void tick(BlockState state, World worldIn, BlockPos pos, Random random) {
        if (!worldIn.isAreaLoaded(pos, 1) && state.get(LIFE_STAGE) == CropLifeStageType.NONE) return; // Forge: prevent loading unloaded chunks when checking neighbor's light
        if (worldIn.getLightSubtracted(pos, 0) >= 9 && isGrowable(worldIn, pos)) {
            int i = this.getAge(state);
            float f = getGrowthChance(this, worldIn, pos);
            if (i < this.getMaxAge()) {
                if (net.minecraftforge.common.ForgeHooks.onCropsGrowPre(worldIn, pos, state, random.nextInt((int)(25.0F / f) + 1) == 0)) {
                    BlockState lastState = state.getBlockState();
                    worldIn.setBlockState(pos, lastState.with(AGE, state.get(AGE) + 1));
                    net.minecraftforge.common.ForgeHooks.onCropsGrowPost(worldIn, pos, state);
                }
            } else if ((CropLifeStageType.checkCropLife(worldIn) == CropLifeStageType.BERRY) && state.get(LIFE_STAGE) != CropLifeStageType.BERRY || state.get(MID_BERRY)) {
                if (net.minecraftforge.common.ForgeHooks.onCropsGrowPre(worldIn, pos, state, random.nextInt((int)(25.0F / f) + 1) == 0) && i == this.getMaxAge()) {
                    BlockState lastState = state.getBlockState();
                    if (state.get(LIFE_STAGE) == CropLifeStageType.BERRY && state.get(MID_BERRY)) {
                        worldIn.setBlockState(pos, lastState.with(MID_BERRY, false));
                    } else if (lastState.get(LIFE_STAGE) != CropLifeStageType.BERRY) {
                        worldIn.setBlockState(pos, lastState.with(MID_BERRY, true).with(LIFE_STAGE, CropLifeStageType.BERRY));
                    }
                    net.minecraftforge.common.ForgeHooks.onCropsGrowPost(worldIn, pos, state);
                }
            }
        }
    }

    @Override
    public void randomTick(BlockState state, World worldIn, BlockPos pos, Random random) {
        super.randomTick(state, worldIn, pos, random);
        if (!worldIn.isRemote && (worldIn.rand.nextInt(8) == 0)) {
            if (CropLifeStageType.checkCropLife(worldIn) == CropLifeStageType.NONE) {
                    worldIn.setBlockState(pos, Blocks.AIR.getDefaultState());
                    if (worldIn.rand.nextInt(4) == 0) {
                        spawnAsEntity(worldIn, pos, new ItemStack(ItemRegistry.elderberries, 1));
                }
            }
        }
    }

    @Override
    public void grow(World worldIn, BlockPos pos, BlockState state) {
        if (isGrowable(worldIn, pos)) {
            BlockState lastState = state.getBlockState();
            if (CropLifeStageType.checkCropLife(worldIn) == CropLifeStageType.BERRY && lastState.get(LIFE_STAGE) != CropLifeStageType.BERRY && isMaxAge(lastState)) {
                worldIn.setBlockState(pos, lastState.with(MID_BERRY, true).with(LIFE_STAGE, CropLifeStageType.BERRY));
            } else if (worldIn.getBlockState(pos).get(MID_BERRY) && isMaxAge(lastState)) {
                worldIn.setBlockState(pos, lastState.with(MID_BERRY, false).with(LIFE_STAGE, CropLifeStageType.BERRY));
            } else {
                int i = this.getAge(state) + this.getBonemealAgeIncrease(worldIn);
                int j = this.getMaxAge();
                if (i > j) {
                    i = j;
                }

                worldIn.setBlockState(pos, lastState.with(AGE, i));
            }
        }
    }

    @Override
    public boolean ticksRandomly(BlockState state) {
        return true;
    }

    @Override
    protected int getBonemealAgeIncrease(World worldIn) {
        return 1;
    }

    protected static float getGrowthChance(Block blockIn, IBlockReader worldIn, BlockPos pos) {
        float f = 5.0F;
        if (worldIn.getLightValue(pos) >= 9) {
            return f;
        }
        else return f/1.2F;
    }

    public static Boolean isOnLeaves(IWorldReader world, BlockPos pos) {
        Direction direction = world.getBlockState(pos).get(FACING).getOpposite();
        return world.getBlockState(pos.offset(direction.getOpposite())).getBlock() instanceof ElderLeavesBlock;
    }

    @Override
    protected boolean isValidGround(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return true;
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        return isOnLeaves(worldIn, pos);
    }

    @Override
    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
        if (entityIn instanceof RavagerEntity && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(worldIn, entityIn)) {
            worldIn.destroyBlock(pos, true);
        }

        super.onEntityCollision(state, worldIn, pos, entityIn);
    }

    /**
     * Whether this IGrowable can grow
     */
    @Override
    public boolean canGrow(IBlockReader worldIn, BlockPos pos, BlockState state, boolean isClient) {
        return !this.isMaxAge(state) && isGrowable((World) worldIn, pos);
    }

    @Override
    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, BlockState state) {
        return isGrowable(worldIn, pos);
    }

    @Override
    public void grow(World worldIn, Random rand, BlockPos pos, BlockState state) {
        this.grow(worldIn, pos, state);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(AGE, FACING, LIFE_STAGE, MID_BERRY);
    }

}
