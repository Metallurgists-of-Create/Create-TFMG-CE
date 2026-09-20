package com.drmangotea.tfmg.registry;

import com.drmangotea.tfmg.TFMG;
import com.drmangotea.tfmg.base.data_storage.BrokenByExplosionCondition;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public class TFMGLootItemConditions {
    public static LootItemConditionType BROKEN_BY_EXPLOSION = register("broken_by_explosion", BrokenByExplosionCondition.CODEC);

    private static LootItemConditionType register(String name, MapCodec<? extends LootItemCondition> codec) {
        return Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, TFMG.asResource(name), new LootItemConditionType(codec));
    }

    public static void prepare() {}
}
