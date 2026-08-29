package com.drmangotea.tfmg.base.fluid;

import com.simibubi.create.foundation.fluid.SmartFluidTank;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Simple tank with insertion and extraction restrictions that can be bypassed manually.
 */
@MethodsReturnNonnullByDefault @ParametersAreNonnullByDefault
public class ForceableFluidTank extends SmartFluidTank {
    private boolean extractionAllowed = true, insertionAllowed = true;

    public ForceableFluidTank(int capacity, Consumer<FluidStack> updateCallback) {
        super(capacity, updateCallback);
    }

    public ForceableFluidTank blockExtraction() {
        this.extractionAllowed = false;
        return this;
    }

    public ForceableFluidTank blockInsertion() {
        this.insertionAllowed = false;
        return this;
    }

    public ForceableFluidTank allowExtraction() {
        this.extractionAllowed = true;
        return this;
    }

    public ForceableFluidTank allowInsertion() {
        this.insertionAllowed = true;
        return this;
    }

    public ForceableFluidTank withValidator(Predicate<FluidStack> validator) {
        this.validator = validator;
        return this;
    }

    public ForceableFluidTank withCapacity(int capacity) {
        this.capacity = capacity;
        return this;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (!extractionAllowed) return FluidStack.EMPTY;
        return super.drain(resource, action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (!extractionAllowed) return FluidStack.EMPTY;
        return super.drain(maxDrain, action);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (!insertionAllowed) return 0;
        return super.fill(resource, action);
    }

    public FluidStack forceDrain(FluidStack resource, FluidAction action) {
        return super.drain(resource, action);
    }

    public FluidStack forceDrain(int maxDrain, FluidAction action) {
        return super.drain(maxDrain, action);
    }

    public int forceFill(FluidStack resource, FluidAction action) {
        return super.fill(resource, action);
    }

    public ForceableFluidTank readFromNBT(HolderLookup.Provider lookupProvider, CompoundTag nbt) {
        this.fluid = FluidStack.parseOptional(lookupProvider, nbt.getCompound("Fluid"));
        return this;
    }

    public CompoundTag writeToNBT(HolderLookup.Provider lookupProvider, CompoundTag nbt) {
        if (!this.fluid.isEmpty()) {
            nbt.put("Fluid", this.fluid.save(lookupProvider));
        }
        return nbt;
    }
}
