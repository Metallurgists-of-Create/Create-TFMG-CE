package com.drmangotea.tfmg.mixin;

import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.drmangotea.tfmg.registry.TFMGLootContextParams;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.loot.LootParams;

@Debug(export = true)
@Mixin(BlockBehaviour.class)
public abstract class BlockBehaviourMixin {
    // It's very silly that this was necessary.
    // Vanilla loot table datapacks aren't capable of checking whether an explosion is responsible for the block's destruction:
    //  - The minecraft:survives_explosion predicate is only useful for drop decay, as it succeeds in the no-explosion case.
    //  - Furthermore, even if it were possible to access the blast radius parameter outside of Java code, it's only included when the explosion decays drops.
    //  - Supplying blast radius for all explosions isn't an option either, because that would make explosions ALWAYS have drop decay.
    @Inject(method = "onExplosionHit", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/world/level/block/state/BlockState;getDrops(Lnet/minecraft/world/level/storage/loot/LootParams$Builder;)Ljava/util/List;"))
    private static void addExplosionToContext(CallbackInfo ci, @Local LootParams.Builder lootParams, @Local Explosion explosion) {
        lootParams.withParameter(TFMGLootContextParams.EXPLOSION, explosion);
    }
}
