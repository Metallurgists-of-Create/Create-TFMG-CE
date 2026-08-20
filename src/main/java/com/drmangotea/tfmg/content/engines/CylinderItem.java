package com.drmangotea.tfmg.content.engines;

import com.drmangotea.tfmg.registry.TFMGDataComponents;
import net.minecraft.world.item.Item;

/**
 * @deprecated Use {@link TFMGDataComponents#ENGINE_CYLINDER}.
 */
@Deprecated(since = "1.2.4", forRemoval = true)
public class CylinderItem extends Item {

    public CylinderItem(Properties properties) {
        super(properties);
    }
}
