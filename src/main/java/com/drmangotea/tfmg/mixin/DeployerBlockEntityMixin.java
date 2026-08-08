package com.drmangotea.tfmg.mixin;

import com.drmangotea.tfmg.content.items.weapons.lithium_blade.LithiumBladeItem;
import com.drmangotea.tfmg.registry.TFMGItems;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DeployerBlockEntity.class, remap = false)
public class DeployerBlockEntityMixin {

    @Shadow
    protected DeployerFakePlayer player;

    /**
     * Handles inventory ticking for the fake player.
     */
    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item.isEmpty())
                continue;
            if (item.is(TFMGItems.LITHIUM_BLADE)) {
                LithiumBladeItem.decrementCharge(item, 1);
            }
        }
    }
}
