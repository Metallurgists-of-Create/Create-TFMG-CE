package com.drmangotea.tfmg.content.machinery.misc.smokestack;

import com.drmangotea.tfmg.base.TFMGUtils;
import com.drmangotea.tfmg.base.fluid.ForceableFluidTank;
import com.drmangotea.tfmg.registry.TFMGBlockEntities;
import com.drmangotea.tfmg.registry.TFMGFluids;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.List;

import static com.drmangotea.tfmg.content.machinery.misc.smokestack.SmokestackBlock.TOP;


public class SmokestackBlockEntity extends SmartBlockEntity {

    int smokeTimer = 0;

    public FluidTank tankInventory;
    protected IFluidHandler fluidCapability;

    protected boolean updateCapability;

    public SmokestackBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        tankInventory = new ForceableFluidTank(8000, this::onFluidStackChanged)
			.blockExtraction() //it makes no sense to extract from an exhaust
			.withValidator((stack) -> stack.getFluid().isSame(TFMGFluids.CARBON_DIOXIDE.getSource()));
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

    @Override
    public void tick() {
        super.tick();
        if (level == null) return;
        if (smokeTimer > 0) {
			TFMGUtils.spawnSmokeParticles(level, getBlockPos());
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
