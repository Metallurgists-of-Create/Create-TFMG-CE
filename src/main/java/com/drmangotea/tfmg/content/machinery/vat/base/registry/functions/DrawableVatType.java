package com.drmangotea.tfmg.content.machinery.vat.base.registry.functions;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

@FunctionalInterface
public interface DrawableVatType {

    void draw(ResourceLocation vatType, GuiGraphics graphics, double mouseX, double mouseY);
}
