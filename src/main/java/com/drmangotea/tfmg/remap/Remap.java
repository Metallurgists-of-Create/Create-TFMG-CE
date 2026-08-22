package com.drmangotea.tfmg.remap;

import com.drmangotea.tfmg.TFMG;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Remap {
    private final String old;
    private final ResourceLocation current;

    private List<ResourceKey<? extends Registry<?>>> registries = new ArrayList<>();

    public Remap(String old, ResourceLocation current) {
        this.old = old;
        this.current = current;
    }

    @SafeVarargs
    public final Remap forTypes(ResourceKey<? extends Registry<?>>... registries) {
        this.registries = Arrays.asList(registries);
        return this;
    }

    public static Remap block(String old, ResourceLocation current) {
        return new Remap(old, current).forTypes(Registries.BLOCK, Registries.ITEM);
    }

    public static Remap item(String old, ResourceLocation current) {
        return new Remap(old, current).forTypes(Registries.ITEM);
    }

    public static Remap fluid(String old, ResourceLocation current) {
        return new Remap(old, current).forTypes(Registries.FLUID);
    }

    public String getOld() {
        return old;
    }

    public ResourceLocation getCurrent() {
        return current;
    }

    public void remap(Registry<?> registry) {
        for (var key : this.registries) {
            if (registry.key() == key) {
                registry.addAlias(TFMG.asResource(getOld()), getCurrent());
                TFMG.LOGGER.info("[TFMG Remapper ({})] Remapped {} to {}", key.location(), TFMG.asResource(getOld()), getCurrent());
            }
        }
    }
}
