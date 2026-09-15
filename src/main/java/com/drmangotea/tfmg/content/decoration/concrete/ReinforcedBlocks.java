package com.drmangotea.tfmg.content.decoration.concrete;

import com.drmangotea.tfmg.registry.TFMGBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public abstract class ReinforcedBlocks {
    public static Block simple(boolean rebar, BlockBehaviour.Properties p) {
        return rebar ? new SimpleReinforcedBlock(p) : new Block(p);
    }

    public static class SimpleReinforcedBlock extends Block implements ReinforcedBlock {
        public SimpleReinforcedBlock(BlockBehaviour.Properties p) {
            super(p);
        }

        @Override
        public BlockState withoutConcrete(BlockState state) {
            return TFMGBlocks.REBAR_BLOCK.getDefaultState();
        }
    }

    public static SlabBlock slab(boolean rebar, BlockBehaviour.Properties p) {
        return rebar ? new ReinforcedSlabBlock(p) : new SlabBlock(p);
    }

    public static class ReinforcedSlabBlock extends SlabBlock implements ReinforcedBlock {
        public ReinforcedSlabBlock(BlockBehaviour.Properties p) {
            super(p);
        }

        @Override
        public BlockState withoutConcrete(BlockState state) {
            return TFMGBlocks.REBAR_FLOOR.getDefaultState();
        }
    }

    public static StairBlock stair(boolean rebar, BlockState s, BlockBehaviour.Properties p) {
        return rebar ? new ReinforcedStairBlock(s, p) : new StairBlock(s, p);
    }

    public static class ReinforcedStairBlock extends StairBlock implements ReinforcedBlock {
        public ReinforcedStairBlock(BlockState s, BlockBehaviour.Properties p) {
            super(s, p);
        }

        @Override
        public BlockState withoutConcrete(BlockState state) {
            return TFMGBlocks.REBAR_STAIRS.getDefaultState()
                .setValue(FACING, state.getValue(FACING))
                .setValue(HALF, state.getValue(HALF));
        }
    }

    public static WallBlock wall(boolean rebar, BlockBehaviour.Properties p) {
        return rebar ? new ReinforcedWallBlock(p) : new WallBlock(p);
    }

    public static class ReinforcedWallBlock extends WallBlock implements ReinforcedBlock {
        public ReinforcedWallBlock(BlockBehaviour.Properties p) {
            super(p);
        }

        @Override
        public BlockState withoutConcrete(BlockState state) {
            return TFMGBlocks.REBAR_WALL.getDefaultState();
        }
    }
}
