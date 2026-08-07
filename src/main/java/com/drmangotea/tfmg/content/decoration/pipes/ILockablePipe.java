package com.drmangotea.tfmg.content.decoration.pipes;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import java.util.Map;

public interface ILockablePipe {

    boolean locked();
    void setLocked(boolean locked);

    default void toggleLock(Player player, Level world, BlockPos pos, BlockState state) {
        world.playSound(player, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.4f, 0.5f);

        setLocked(!locked());
        if (locked())
            return;

        BlockState newState;
        FluidTransportBehaviour.cacheFlows(world, pos);
        newState = updatePipe(world, pos, state).setValue(BlockStateProperties.WATERLOGGED, state.getValue(BlockStateProperties.WATERLOGGED));
        world.setBlock(pos, newState, 3);
        FluidTransportBehaviour.loadFlows(world, pos);
    }

    default BlockState updatePipe(LevelAccessor world, BlockPos pos, BlockState state) {
        Direction side = Direction.UP;
        Map<Direction, BooleanProperty> facingToPropertyMap = FluidPipeBlock.PROPERTY_BY_DIRECTION;
        return AllBlocks.FLUID_PIPE.get()
                .updateBlockState(state.getBlock().defaultBlockState()
                        .setValue(facingToPropertyMap.get(side), true)
                        .setValue(facingToPropertyMap.get(side.getOpposite()), true), side, null, world, pos);
    }
}
