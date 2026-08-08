package com.drmangotea.tfmg.content.items.handlers;

import com.simibubi.create.foundation.fluid.SmartFluidTank;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.function.Predicate;

public class ItemFluidTank extends SmartFluidTank {

    public ItemFluidTank(int capacity, Predicate<FluidStack> validator) {
        super(capacity, (f) -> {});
        this.validator = validator;
    }


}
