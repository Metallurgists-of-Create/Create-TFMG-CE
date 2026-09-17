package com.drmangotea.tfmg.content.decoration.concrete;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.extensions.IBlockExtension;

public interface ReinforcedBlock extends IBlockExtension {
    abstract BlockState withoutConcrete(BlockState state);

    @Override
    default void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        BlockState newState = withoutConcrete(state).setValue(ConcreteloggedBlock.CONCRETELOGGED, false);
        level.setBlock(pos, newState, Block.UPDATE_NEIGHBORS | Block.UPDATE_CLIENTS);
    }
}
