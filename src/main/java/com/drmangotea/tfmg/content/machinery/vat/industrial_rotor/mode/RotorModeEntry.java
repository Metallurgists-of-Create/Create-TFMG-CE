package com.drmangotea.tfmg.content.machinery.vat.industrial_rotor.mode;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.neoforged.neoforge.registries.DeferredHolder;

public class RotorModeEntry<T extends RotorMode> extends RegistryEntry<RotorMode, T> {
    public RotorModeEntry(AbstractRegistrate<?> owner, DeferredHolder<RotorMode, T> key) {
        super(owner, key);
    }

    public static <T extends RotorMode> RotorModeEntry<T> cast(RegistryEntry<RotorMode, T> entry) {
        return RegistryEntry.cast(RotorModeEntry.class, entry);
    }
}
