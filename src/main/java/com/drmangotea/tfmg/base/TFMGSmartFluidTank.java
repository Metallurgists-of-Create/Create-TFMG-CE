package com.drmangotea.tfmg.base;

import com.simibubi.create.foundation.fluid.SmartFluidTank;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class TFMGSmartFluidTank extends SmartFluidTank {
	final boolean extractionAllowed, insertionAllowed;
	final Fluid validFluid;
	
	public TFMGSmartFluidTank(int capacity, boolean extractionAllowed, boolean insertionAllowed, Consumer<FluidStack> updateCallback, @Nullable Fluid validFluid) {
		super(capacity, updateCallback);
		this.extractionAllowed = extractionAllowed;
		this.insertionAllowed = insertionAllowed;
		this.validFluid = validFluid;
	}
	
	public static TFMGSmartFluidTank inputOnly (int capacity, Consumer<FluidStack> updateCallback) {
		return new TFMGSmartFluidTank(capacity, false, true, updateCallback, null);
	}
	
	public static TFMGSmartFluidTank outputOnly (int capacity, Consumer<FluidStack> updateCallback) {
		return new TFMGSmartFluidTank(capacity, true, false, updateCallback, null);
	}
	
	public static TFMGSmartFluidTank IO (int capacity, Consumer<FluidStack> updateCallback) {
		//anything that uses this can probably just use a regular smart fluid tank, tbh
		return new TFMGSmartFluidTank(capacity, true, true, updateCallback, null);
	}
	
	@Override
	public boolean isFluidValid(FluidStack stack) {
		if (validFluid == null) return true;
		return stack.getFluid().isSame(validFluid);
	}
	
	@Override @Nonnull
	public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
		if (!extractionAllowed) return FluidStack.EMPTY;
		return super.drain(resource, action);
	}
	
	@Override @Nonnull
	public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
		if (!extractionAllowed) return FluidStack.EMPTY;
		return super.drain(maxDrain, action);
	}
	
	public FluidStack forceDrain(FluidStack resource, IFluidHandler.FluidAction action) {
		return super.drain(resource, action);
	}
	
	public FluidStack forceDrain(int maxDrain, IFluidHandler.FluidAction action) {
		return super.drain(maxDrain, action);
	}
	
	@Override
	public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
		if (!insertionAllowed) return 0;
		return super.fill(resource, action);
	}
	
	public int forceFill (FluidStack resource, IFluidHandler.FluidAction action) {
		return super.fill(resource, action);
	}
}