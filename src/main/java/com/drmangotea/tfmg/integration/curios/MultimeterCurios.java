package com.drmangotea.tfmg.integration.curios;

import com.drmangotea.tfmg.content.electricity.measurement.MultimeterItem;
import com.simibubi.create.compat.Mods;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Optional;

public class MultimeterCurios {

    public static MultimeterItem getHeldByPlayer (Player player) {
		if (player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof MultimeterItem meter)
			return meter;
		if (player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof MultimeterItem meter)
			return meter;
        if (!Mods.CURIOS.isLoaded()) return null;
        return LoadedOnly.getHeldByPlayer(player);
    }

    public static class LoadedOnly {

        public static MultimeterItem getHeldByPlayer (Player player) {
			Optional<ICuriosItemHandler> curios = CuriosApi.getCuriosInventory(player);
			if (curios.isEmpty()) return null;
			
			Optional<ICurioStacksHandler> maybeBelt = curios.get().getStacksHandler("belt");
			if (maybeBelt.isEmpty()) return null;
			
			IDynamicStackHandler belt = maybeBelt.get().getStacks();
			int slots = belt.getSlots();
			for (int i = 0; i < slots; i++) {
				ItemStack stack = belt.getStackInSlot(i);
				if (stack.getItem() instanceof MultimeterItem meter)
					return meter;
			}
			return null;
        }
    }
}
