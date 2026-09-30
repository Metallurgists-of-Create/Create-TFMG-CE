package com.drmangotea.tfmg.content.electricity.base;

import com.drmangotea.tfmg.content.electricity.measurement.MultimeterItem;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

public interface IHaveMultimeterInformation extends IHaveGoggleInformation {
	/**
	 * Adds Multimeter Tooltip to UI
	 */
	@Override @OnlyIn(Dist.CLIENT)
	default boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		if (Minecraft.getInstance().player == null || !MultimeterItem.isHeldByPlayer(Minecraft.getInstance().player))
			return false;
		return makeMultimeterTooltip(tooltip, isPlayerSneaking);
	}
	
	/**
	 * Populates the Multimeter Tooltip from the IElectrics' Data
	 * @param tooltip The Tooltip to Populate
	 * @param isPlayerSneaking Whether the Player is Sneaking
	 * @return Whether the Tooltip should be displayed
	 */
	boolean makeMultimeterTooltip(List<Component> tooltip, boolean isPlayerSneaking);
}
