package com.drmangotea.tfmg.content.electricity.experimental.content.devices;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class DebugResistorBlockEntity extends ResistiveLoadBlockEntity {
    public DebugResistorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
}
