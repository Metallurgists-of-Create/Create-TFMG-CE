package com.drmangotea.tfmg.base.dyes;

import com.tterrag.registrate.util.entry.FluidEntry;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class DyedFluidList<T extends BaseFlowingFluid> implements Iterable<FluidEntry<T>> {

    private static final int COLOR_AMOUNT = DyeColor.values().length;

    private final FluidEntry<?>[] values = new FluidEntry<?>[COLOR_AMOUNT];

    public DyedFluidList(Function<DyeColor, FluidEntry<? extends T>> filler) {
        for (DyeColor color : DyeColor.values()) {
            values[color.ordinal()] = filler.apply(color);
        }
    }

    @SuppressWarnings("unchecked")
    public FluidEntry<T> get(DyeColor color) {
        return (FluidEntry<T>) values[color.ordinal()];
    }

    public Fluid getSource(DyeColor color) {
        return get(color).getSource();
    }

    public boolean contains(Fluid fluid) {
        for (FluidEntry<?> entry : values) {
            if (entry.is(fluid)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public FluidEntry<T>[] toArray() {
        return (FluidEntry<T>[]) Arrays.copyOf(values, values.length);
    }

    @Override
    public Iterator<FluidEntry<T>> iterator() {
        return new Iterator<>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < values.length;
            }

            @SuppressWarnings("unchecked")
            @Override
            public FluidEntry<T> next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                return (FluidEntry<T>) values[index++];
            }
        };
    }
}
