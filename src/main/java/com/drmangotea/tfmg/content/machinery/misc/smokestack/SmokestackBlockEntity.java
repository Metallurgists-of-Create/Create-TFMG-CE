package com.drmangotea.tfmg.content.machinery.misc.smokestack;

import com.drmangotea.tfmg.TFMG;
import com.drmangotea.tfmg.registry.TFMGBlockEntities;
import com.drmangotea.tfmg.registry.TFMGFluids;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.List;
import java.util.Random;

import static com.drmangotea.tfmg.content.machinery.misc.smokestack.SmokestackBlock.TOP;


public class SmokestackBlockEntity extends SmartBlockEntity {

    int smokeTimer = 0;

    public FluidTank tankInventory;
    protected IFluidHandler fluidCapability;

    protected boolean updateCapability;

    public SmokestackBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        tankInventory = new SmartFluidTank(8000, this::onFluidStackChanged) {
            @Override
            public boolean isFluidValid(FluidStack stack) {
                return stack.getFluid().isSame(TFMGFluids.CARBON_DIOXIDE.getSource());
            }
        };
        fluidCapability = tankInventory;
        updateCapability = false;
        refreshCapability();
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                TFMGBlockEntities.SMOKESTACK.get(),
                (be, context) -> be.fluidCapability
        );
    }

    @Override
    public void invalidate() {
        super.invalidate();
        invalidateCapabilities();
    }

    public void refreshCapability() {
        fluidCapability = tankInventory;
        invalidateCapabilities();
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound,registries , clientPacket);
        tankInventory.readFromNBT(registries,compound.getCompound("TankContent"));
        smokeTimer = compound.getInt("Timer");
        updateCapability = true;
    }

    protected void onFluidStackChanged(FluidStack newFluidStack) {
        if (!hasLevel())
            return;
        setChanged();
        sendData();
    }

    public static void makeParticles(Level level, BlockPos pos) {
        Random random = TFMG.RANDOM;
        int shouldSpawnSmoke = random.nextInt(7);
        if (shouldSpawnSmoke == 0) {
            level.addParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, pos.getX() + random.nextFloat(1), pos.getY() + 1, pos.getZ() + random.nextFloat(1), 0.0D, 0.08D, 0.0D);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null) return;
        if (smokeTimer > 0) {
            makeParticles(level, getBlockPos());
            smokeTimer--;
        }

        if (updateCapability) {
            updateCapability = false;
            refreshCapability();
        }

        if (tankInventory.isEmpty())
            return;

        if (getBlockState().getValue(TOP)) {
            tankInventory.drain(150, IFluidHandler.FluidAction.EXECUTE);
            smokeTimer = 40;
        }

        if (level != null && level.getBlockEntity(getBlockPos().above()) instanceof SmokestackBlockEntity be) {
            int transferAmount = Math.min(tankInventory.getFluidAmount(), be.tankInventory.getCapacity() - be.tankInventory.getFluidAmount());
            tankInventory.drain(transferAmount, IFluidHandler.FluidAction.EXECUTE);
            be.tankInventory.fill(new FluidStack(TFMGFluids.CARBON_DIOXIDE.get(), transferAmount), IFluidHandler.FluidAction.EXECUTE);
        }
    }

    @Override
    public void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound,registries , clientPacket);
        compound.put("TankContent", tankInventory.writeToNBT(registries,new CompoundTag()));
        compound.putInt("Timer", smokeTimer);
    }


    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }
}
