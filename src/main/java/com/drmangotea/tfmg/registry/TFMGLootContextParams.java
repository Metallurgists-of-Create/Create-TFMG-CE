package com.drmangotea.tfmg.registry;

import com.drmangotea.tfmg.TFMG;

import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public class TFMGLootContextParams {
    public static final LootContextParam<Explosion> EXPLOSION = create("explosion");

    private static <T> LootContextParam<T> create(String name) {
        return new LootContextParam<>(TFMG.asResource(name));
    }
}
