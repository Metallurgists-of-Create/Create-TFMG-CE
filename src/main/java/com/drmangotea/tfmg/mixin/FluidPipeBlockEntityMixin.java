package com.drmangotea.tfmg.mixin;

import com.drmangotea.tfmg.content.decoration.pipes.ILockablePipe;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(FluidPipeBlockEntity.class)
public abstract class FluidPipeBlockEntityMixin extends SmartBlockEntity implements ILockablePipe {
    @Unique
    private boolean tfmg$locked = false;

    public FluidPipeBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public boolean locked() {
        return tfmg$locked;
    }

    @Override
    public void setLocked(boolean locked) {
        tfmg$locked = locked;
    }

    @Override
    public void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        compound.putBoolean("Locked", tfmg$locked);
        super.write(compound,registries , clientPacket);
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound,registries , clientPacket);
        tfmg$locked = compound.getBoolean("Locked");
    }
}
