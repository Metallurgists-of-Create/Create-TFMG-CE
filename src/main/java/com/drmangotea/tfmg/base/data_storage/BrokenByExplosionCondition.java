package com.drmangotea.tfmg.base.data_storage;

import java.util.Set;

import org.spongepowered.include.com.google.common.collect.ImmutableSet;

import com.drmangotea.tfmg.registry.TFMGLootContextParams;
import com.drmangotea.tfmg.registry.TFMGLootItemConditions;
import com.mojang.serialization.MapCodec;

import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public class BrokenByExplosionCondition implements LootItemCondition {
    private static final BrokenByExplosionCondition INSTANCE = new BrokenByExplosionCondition();
    public static final MapCodec<BrokenByExplosionCondition> CODEC = MapCodec.unit(INSTANCE);

    private BrokenByExplosionCondition() {}

    @Override
    public LootItemConditionType getType() {
        return TFMGLootItemConditions.BROKEN_BY_EXPLOSION;
    }

    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(TFMGLootContextParams.EXPLOSION);
    }

    public boolean test(LootContext ctx) {
        return ctx.hasParam(TFMGLootContextParams.EXPLOSION);
    }

    public static LootItemCondition.Builder brokenByExplosion() {
        return () -> INSTANCE;
    }
}
