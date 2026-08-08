package com.drmangotea.tfmg.registry;


import com.drmangotea.tfmg.TFMG;
import com.mojang.blaze3d.systems.RenderSystem;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.gui.element.ScreenElement;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;


public enum TFMGGuiTextures implements ScreenElement {


    // SCREENS
    ELECTRICIANS_WRENCH("electricians_wrench", 0, 0, 188, 101),
    ENGINE_CONTROLLER("engine_controller", 0, 0, 179, 109),
    // JEI
    DISTILLATION_TOWER_TOP("distillation_tower", 0, 0, 44, 12),
    DISTILLATION_TOWER_MIDDLE("distillation_tower", 0, 12, 44, 24),
    DISTILLATION_TOWER_BOTTOM("distillation_tower", 0, 36, 44, 24),
    DISTILLATION_TOWER_FIRE("distillation_tower", 0, 60, 44, 12),
    BLAST_STOVE("distillation_tower", 60, 0, 45, 105),
    VAT("chemical_vat", 0, 0, 110, 84),
    VAT_MACHINE("chemical_vat", 112, 0, 24, 24),
    SLOT("chemical_vat", 112, 24, 20, 20),
    MIXER("chemical_vat", 136, 0, 38, 29),
    CENTRIFUGE("chemical_vat", 143, 30, 24, 29),
    ELECTRODE("chemical_vat", 189, 0, 8, 29),
    GRAPHITE_ELECTRODE("chemical_vat", 176, 0, 8, 29),
    FIREPROOF_BRICK_OVERLAY("chemical_vat", 0, 84, 96, 72),
    CAST_IRON_VAT_OVERLAY("chemical_vat", 0, 156, 110, 84),
    VAT_FREEZER("chemical_vat",112,80,9,9),
    VAT_HEATER("chemical_vat", 112, 89, 9, 9),
    VAT_SUPERHEATER("chemical_vat", 112, 98, 9, 9),
    VAT_BAROMETER("chemical_vat",211,3,42,42),
    //im so sorry
    VAT_BAROMETER_NEEDLE_OFF("chemical_vat",202,75,15,8),
    VAT_BAROMETER_NEEDLE_LOWNINE("chemical_vat",201,232,16,5),
    VAT_BAROMETER_NEEDLE_HIGHNINE("chemical_vat",231,232,16,5),
    VAT_BAROMETER_NEEDLE_LOWEIGHT("chemical_vat",201,214,16,7),
    VAT_BAROMETER_NEEDLE_HIGHEIGHT("chemical_vat",231,214,16,7),
    VAT_BAROMETER_NEEDLE_LOWSEVEN("chemical_vat",203,195,14,10),
    VAT_BAROMETER_NEEDLE_HIGHSEVEN("chemical_vat",231,195,14,10),
    VAT_BAROMETER_NEEDLE_LOWSIX("chemical_vat",204,177,13,12),
    VAT_BAROMETER_NEEDLE_HIGHSIX("chemical_vat",231,177,13,12),
    VAT_BAROMETER_NEEDLE_LOWFIVE("chemical_vat",205,159,12,14),
    VAT_BAROMETER_NEEDLE_HIGHFIVE("chemical_vat",231,159,12,14),
    VAT_BAROMETER_NEEDLE_LOWFOUR("chemical_vat",207,143,10,14),
    VAT_BAROMETER_NEEDLE_HIGHFOUR("chemical_vat",231,143,10,14),
    VAT_BAROMETER_NEEDLE_LOWTHREE("chemical_vat",209,126,8,15),
    VAT_BAROMETER_NEEDLE_HIGHTHREE("chemical_vat",231,126,8,15),
    VAT_BAROMETER_NEEDLE_LOWTWO("chemical_vat",211,109,6,16),
    VAT_BAROMETER_NEEDLE_HIGHTWO("chemical_vat",231,109,6,16),
    VAT_BAROMETER_NEEDLE_LOWONE("chemical_vat",213,92,4,17),
    VAT_BAROMETER_NEEDLE_HIGHONE("chemical_vat",231,92,4,17),
    VAT_BAROMETER_NEEDLE_ZERO("chemical_vat",231,60,2,17),
    ;




    public final ResourceLocation location;
    public int width, height;
    public int startX, startY;

    private TFMGGuiTextures(String location, int width, int height) {
        this(location, 0, 0, width, height);
    }

    private TFMGGuiTextures(int startX, int startY) {
        this("icons", startX * 16, startY * 16, 16, 16);
    }

    private TFMGGuiTextures(String location, int startX, int startY, int width, int height) {
        this(TFMG.MOD_ID, location, startX, startY, width, height);
    }

    private TFMGGuiTextures(String namespace, String location, int startX, int startY, int width, int height) {
        this.location = ResourceLocation.fromNamespaceAndPath(namespace, "textures/gui/" + location + ".png");
        this.width = width;
        this.height = height;
        this.startX = startX;
        this.startY = startY;
    }

    @OnlyIn(Dist.CLIENT)
    public void bind() {
        RenderSystem.setShaderTexture(0, location);
    }

    @OnlyIn(Dist.CLIENT)
    public void render(GuiGraphics graphics, int x, int y) {
        graphics.blit(location, x, y, startX, startY, width, height);
    }

    @OnlyIn(Dist.CLIENT)
    public void render(GuiGraphics graphics, int x, int y, Color c) {
        bind();
        UIRenderHelper.drawColoredTexture(graphics, c, x, y, startX, startY, width, height);
    }

}
