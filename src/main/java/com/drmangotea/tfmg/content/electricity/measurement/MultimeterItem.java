package com.drmangotea.tfmg.content.electricity.measurement;

import net.minecraft.world.item.Item;

public class MultimeterItem extends Item {
	public final int color;
	
	public MultimeterItem(Properties itemProperties, int color) {
		super(itemProperties);
		this.color = color;
	}


}
