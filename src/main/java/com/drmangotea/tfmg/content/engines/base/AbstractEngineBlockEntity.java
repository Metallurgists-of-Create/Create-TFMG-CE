package com.drmangotea.tfmg.content.engines.base;

import com.drmangotea.tfmg.config.TFMGConfigs;
import com.drmangotea.tfmg.content.electricity.base.KineticElectricBlockEntity;
import com.drmangotea.tfmg.content.engines.fuel.EngineFuel;
import com.drmangotea.tfmg.registry.TFMGBlockEntities;
import com.drmangotea.tfmg.registry.TFMGFluids;
import com.drmangotea.tfmg.registry.TFMGTags;
import com.simibubi.create.foundation.fluid.CombinedTankWrapper;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.List;
import java.util.function.Predicate;

public abstract class AbstractEngineBlockEntity extends KineticElectricBlockEntity {

    //

    public EngineFluidTank fuelTank;
    public EngineFluidTank exhaustTank;
    public IFluidHandler fluidCapability;
    //

    //
    public float rpm = 0;
    //
    public boolean reverse = false;
    //
    public float highestSignal;
    public int signal;
    //
    public BlockPos engineController;
    //

    public float torque = 0;
    public boolean signalChanged;
    //
    public boolean drainFuel = true;


    public AbstractEngineBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        setLazyTickRate(10);
        fuelTank = new EngineFluidTank(8000, false, true, f -> tankUpdated(f, true), TFMGTags.TFMGFluidTags.AIR.tag);
        exhaustTank = new EngineFluidTank(8000, true, false, f -> tankUpdated(f, false));
        fluidCapability = new CombinedTankWrapper(fuelTank, exhaustTank);

        refreshCapability();
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                TFMGBlockEntities.REGULAR_ENGINE.get(),
                (be, context) -> be.fluidCapability
        );
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                TFMGBlockEntities.TURBINE_ENGINE.get(),
                (be, context) -> be.fluidCapability
        );
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                TFMGBlockEntities.RADIAL_ENGINE.get(),
                (be, context) -> be.fluidCapability
        );

    }


    @Override
    public void tick() {
        if (signalChanged) {
            signalChanged = false;
            analogSignalChanged();
        }
        super.tick();
    }

    public void tankUpdated(FluidStack stack, boolean fuelTank) {
        if (fuelTank && stack.isEmpty()) {
            rpm = 0;
            updateRotation();
            analogSignalChanged();
        }
        sendData();
        setChanged();
    }

    public boolean hasEngineController() {
        return engineController != null;
    }

    @Override
    public void updateNetwork() {
        super.updateNetwork();
    }

    protected void analogSignalChanged() {
        if (level == null) return;
        if (hasEngineController()) {
            return;
        }
        int newSignal = level.getBestNeighborSignal(getBlockPos());
        signal = newSignal;
        newSignal = Math.max(level.getBestNeighborSignal(getBlockPos()), newSignal);
        highestSignal = newSignal / 15f;
        updateRotation();

    }


    @Override
    public void lazyTick() {
        super.lazyTick();

        neighbourChanged();
        if(drainFuel) {
            manageFuelAndExhaust();
        } else drainFuel = true;
    }

    public void manageFuelAndExhaust() {
        exhaustTank.forceFill(new FluidStack(TFMGFluids.CARBON_DIOXIDE.get(), Math.min(300, getFuelConsumption())), IFluidHandler.FluidAction.EXECUTE);

            fuelTank.forceDrain(getFuelConsumption(), IFluidHandler.FluidAction.EXECUTE);

            if (fuelTank.isEmpty())
                updateRotation();

            drainFuel = false;

    }

    @Override
    public boolean makeMultimeterTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return false;
    }


    public float getSpeedEfficiency() {
        if (rpm >= 6000)
            return 1;

        return 1 / (0.08f * (rpm / 1000) + 0.5f);
    }


    public abstract Predicate<FluidStack> validFuels();

    public void onUpdated() {
    }


    public boolean canWork() {
        return !fuelTank.isEmpty() && exhaustTank.getSpace() != 0;
    }

    public void updateRotation() {
    }

    public abstract float efficiencyModifier();

    public abstract float speedModifier();

    public abstract float torqueModifier();


    public EngineFuel getFuelType() {
        if (this.level == null) return EngineFuel.EMPTY;
        FluidStack contained = fuelTank.getFluid();
        return EngineFuel.createForType(this.level.registryAccess(), contained);
    }

    public void refreshCapability() {
        fluidCapability = this.handlerForCapability();
        invalidateCapabilities();
    }

    public IFluidHandler handlerForCapability() {
        return new CombinedTankWrapper(fuelTank, exhaustTank);
    }


    public int getMaxLength() {
        return TFMGConfigs.common().machines.engineMaxLength.get();
    }

    public void changeDirection() {
        playInsertionSound();
        reverse = !reverse;
        updateRotation();
    }

    public void dropItem(ItemStack stack) {
        if (level == null) return;
        Vec3 dropVec = VecHelper.getCenterOf(worldPosition).add(0, 0.3f, 0);
        ItemEntity dropped = new ItemEntity(level, dropVec.x, dropVec.y, dropVec.z, stack);
        dropped.setDefaultPickUpDelay();
        dropped.setDeltaMovement(0, 0.15f, 0);
        level.addFreshEntity(dropped);
    }


    public void playInsertionSound() {
        if (level == null) return;
        level.playSound(null, getBlockPos(), SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.4f, 0.5f);
    }

    public void playRemovalSound() {
        if (level == null) return;
        level.playSound(null, getBlockPos(), SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.4f, 0.5f);
    }


    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);

        reverse = compound.getBoolean("Reverse");
        signal = compound.getInt("Signal") + 1;
        if (hasEngineController())
            engineController = NbtUtils.readBlockPos(compound, "EngineController").orElse(getBlockPos());

        fuelTank.readFromNBT(registries, compound.getCompound("FuelTank"));
        exhaustTank.readFromNBT(registries, compound.getCompound("ExhaustTank"));


        updateRotation();
        updateGeneratedRotation();


    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);

        compound.putBoolean("Reverse", reverse);
        compound.putInt("Signal", signal);
        if (hasEngineController()) {
            compound.put("EngineController", NbtUtils.writeBlockPos(engineController));
        }
        compound.put("FuelTank", fuelTank.writeToNBT(registries, new CompoundTag()));
        compound.put("ExhaustTank", exhaustTank.writeToNBT(registries, new CompoundTag()));

    }

    public abstract int getFuelConsumption();

    @Override
    public void onPlaced() {
        super.onPlaced();
    }

    public void neighbourChanged() {
        if (level == null)
            return;
        int power = level.getBestNeighborSignal(getBlockPos());
        if (power != this.signal)
            this.signalChanged = true;
    }
}
