package com.drmangotea.tfmg.recipes.jei;

import com.drmangotea.tfmg.base.lang.TFMGLang;
import com.drmangotea.tfmg.recipes.VatMachineRecipe;
import com.drmangotea.tfmg.registry.TFMGGuiTextures;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.item.ItemHelper;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.createmod.catnip.data.Pair;
import net.createmod.ponder.api.PonderPalette;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ChemicalVatCategory extends CreateRecipeCategory<VatMachineRecipe> {

    public ChemicalVatCategory(Info<VatMachineRecipe> info) {
        super(info);
    }

    public void setRecipe(IRecipeLayoutBuilder builder, VatMachineRecipe recipe, IFocusGroup focuses) {
        int fluidCount = recipe.getFluidIngredients().size();
        int pos = 55;
        int width = ((fluidCount) * 20) / 2;
        int movement = fluidCount != 4 ? 1 : 0;
        if (fluidCount == 1)
            movement = 2;
        for (int i = 0; i < fluidCount; i++) {

            addFluidSlot(builder, pos - width + movement, recipe.getIngredients().isEmpty() ? 72 : 85, recipe.getFluidIngredients().get(i));

            pos += 21;
        }
        List<Pair<Ingredient, MutableInt>> condensedIngredients = ItemHelper.condenseIngredients(recipe.getIngredients());

        int itemCount = condensedIngredients.size();
        int itemPos = 55;
        int itemWidth = ((itemCount) * 20) / 2;
        int itemMovement = itemCount != 4 ? 1 : 0;
        if (itemCount == 1)
            itemMovement = 2;
        for (Pair<Ingredient, MutableInt> pair : condensedIngredients) {
            List<ItemStack> stacks = new ArrayList<>();
            for (ItemStack itemStack : pair.getFirst().getItems()) {
                ItemStack copy = itemStack.copy();
                copy.setCount(pair.getSecond().getValue());
                stacks.add(copy);
            }
            builder.addSlot(RecipeIngredientRole.INPUT, itemPos - itemWidth + itemMovement, recipe.getFluidIngredients().isEmpty() ? 72 : 64).setBackground(getRenderedSlot(), -1, -1).addItemStacks(stacks);

            itemPos += 21;
        }
        /////////////////////////////

        int fluidResultPos = 106;

        for (int i = 0; i < recipe.getFluidResults().size(); i++) {

            addFluidSlot(builder, 150, fluidResultPos, recipe.getFluidResults().get(i));

            fluidResultPos -= 21;
        }

        int itemResultPos = 106;

        for (int i = 0; i < recipe.getRollableResults().size(); i++) {
            ProcessingOutput output = recipe.getRollableResults().get(i);
            builder
                    .addSlot(RecipeIngredientRole.OUTPUT, 128, itemResultPos)
                    .setBackground(getRenderedSlot(output), -1, -1)
                    .addItemStack(output.getStack())
                    .addRichTooltipCallback(addStochasticTooltip(output))
            ;

            itemResultPos -= 21;
        }
    }

    public void draw(VatMachineRecipe recipe, IRecipeSlotsView iRecipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        List<String> machines = recipe.machines;
        List<String> allowedVatTypes = recipe.allowedVatTypes;
        TFMGGuiTextures.VAT.render(graphics, 0, 24);
        drawVatTypes(allowedVatTypes, graphics);
        drawSprites(machines, graphics);

        renderPressure(recipe.pressure, graphics);

        renderHeated(recipe.heatLevel, graphics);

        int pos = 55;
        int width = ((recipe.getFluidIngredients().size()) * 21) / 2;
        for (int i = 0; i < recipe.getFluidIngredients().size(); i++) {
            TFMGGuiTextures.SLOT.render(graphics, pos - width, recipe.getIngredients().isEmpty() ? 70 : 83);
            pos += 21;
        }
        int posItem = 55;
        List<Pair<Ingredient, MutableInt>> condensedIngredients = ItemHelper.condenseIngredients(recipe.getIngredients());
        int widthItem = ((condensedIngredients.size()) * 21) / 2;
        for (int i = 0; i < condensedIngredients.size(); i++) {
            TFMGGuiTextures.SLOT.render(graphics, posItem - widthItem, recipe.getFluidIngredients().isEmpty() ? 70 : 62);
            posItem += 21;
        }
    }

    @Override
    @NotNull
    public List<Component> getTooltipStrings(VatMachineRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        List<Component> tooltip = new ArrayList<>();
        int xOffset = -7;
        int minX = 150 + xOffset;
        int maxX = minX + 18;
        int minY = 90;
        int maxY = minY + 18;

        int pressure = recipe.pressure;

        if (mouseY > -3 && mouseY < 43 && mouseX > 127 && mouseX < 170) {
            if(pressure != 0) {
                int colour = 0x5b6ee1;
                if (pressure > -8 && pressure < -3) colour = 0x5fcde4;
                if (pressure > -4 && pressure < 3) colour = 0x37946e;
                if (pressure > 2 && pressure < 8) colour = 0xd95763;
                if (pressure > 7 && pressure < 10) colour = 0x9b1c1c;

                tooltip.add(TFMGLang.translate("recipe.vat.pressure", pressure).component()
                        .withColor(colour));
            } else {
                tooltip.add(TFMGLang.translate("recipe.vat.pressure.none").component()
                        .withStyle(ChatFormatting.GRAY));
            }
        }

        boolean showHeat;
        if (recipe.heatLevel > 9) {
            showHeat = mouseY > 109 && mouseY < 118 && mouseX > 10 && mouseX < 100;
        } else if (recipe.heatLevel > 0) {
            showHeat = mouseY > 109 && mouseY < 118 && mouseX > 10 && mouseX < (recipe.heatLevel * 10) + 10;
        } else {
            showHeat = mouseY > 109 && mouseY < 118 && mouseX > 10 && mouseX < (Math.abs(recipe.heatLevel) * 10) + 10;
        }

        if (showHeat) {
            int colour = recipe.heatLevel <= 0 ? 0x9eccfb : recipe.heatLevel > 9 ? 0x425bf5 : 0xd17800;
            tooltip.add(TFMGLang.translate("recipe.vat.heat", recipe.heatLevel).component()
                    .withColor(colour));
        }


        List<String> machines = recipe.machines;

        if (machines.contains("tfmg:mixing")) {
            if (mouseY > -3 && mouseY < 60 && mouseX > 43 && mouseX < 67) {
                tooltip.add(TFMGLang.translate("recipe.vat.mixing").component()
                        .withColor(PonderPalette.INPUT.getColor()));
            }
        }
        if (machines.contains("tfmg:centrifuge")) {
            if (mouseY > -3 && mouseY < 60 && mouseX > 43 && mouseX < 67) {
                tooltip.add(TFMGLang.translate("recipe.vat.centrifuge").component()
                        .withColor(PonderPalette.INPUT.getColor()));
            }
        }
        if (machines.contains("tfmg:electrode")) {
            boolean xCheck = mouseX > 11 && mouseX < 35 || mouseX > 75 && mouseX < 99;
            if (mouseY > -3 && mouseY < 60 && xCheck) {
                tooltip.add(TFMGLang.translate("recipe.vat.electrode").component()
                        .withColor(PonderPalette.INPUT.getColor()));
            }
        }
        if (machines.contains("tfmg:graphite_electrode")) {
            if (mouseY > -3 && mouseY < 60 && mouseX > 11 && mouseX < 99) {
                tooltip.add(TFMGLang.translate("recipe.vat.graphite_electrode").component()
                        .withColor(PonderPalette.INPUT.getColor()));
            }
        }

        return tooltip;
    }

    private void renderPressure(int pressure, GuiGraphics graphics) {
        TFMGGuiTextures.VAT_BAROMETER.render(graphics, 128, 0);
        TFMGGuiTextures spritemap = TFMGGuiTextures.VAT_BAROMETER_NEEDLE;
        if (pressure == 0) {
            spritemap.render(graphics, 125, -3, 0, 0, 48, 48);
        } else {
            int xOffset = pressure < 0 ? 0 : 48;
            int yOffset = 48 + (Math.abs(pressure) * 48);
            spritemap.render(graphics,  125, -3, xOffset, yOffset, 48, 48);
        }
    }

    private void renderHeated(int heatLevel, GuiGraphics graphics) {
        if(heatLevel != 0) {
            if (heatLevel > 9) {
                for (int i = 9; i > 0; i--) {
                    if (heatLevel >= 9 + i) {
                        TFMGGuiTextures.VAT_SUPERHEATER.render(graphics, i * 10, 109);
                    } else {
                        TFMGGuiTextures.VAT_HEATER.render(graphics, i * 10, 109);
                    }
                }
            } else if (heatLevel > 0) {
                for (int i = heatLevel; i > 0; i--) {
                    TFMGGuiTextures.VAT_HEATER.render(graphics, i * 10, 109);
                }
            } else {
                for (int i = -1 * heatLevel; i > 0; i--) {
                    TFMGGuiTextures.VAT_FREEZER.render(graphics, i * 10, 109);
                }
            }
        }
    }

    private void drawVatTypes(List<String> allowedVatTypes, GuiGraphics graphics) {
        if (allowedVatTypes.contains("tfmg:firebrick_lined_vat") && allowedVatTypes.size() == 1) {
            TFMGGuiTextures.FIREPROOF_BRICK_OVERLAY.render(graphics, 55 - 48, 32);
        }
        if (allowedVatTypes.contains("tfmg:cast_iron_vat") && allowedVatTypes.size() == 1) {
            TFMGGuiTextures.CAST_IRON_VAT_OVERLAY.render(graphics, 0, 24);
        }
    }

    private void drawSprites(List<String> machines, GuiGraphics graphics) {
        if (machines.contains("tfmg:mixing")) {
            TFMGGuiTextures.VAT_MACHINE.render(graphics, 55 - 12, 0);
            TFMGGuiTextures.MIXER.render(graphics, 55 - 19, 32);
        }
        if (machines.contains("tfmg:centrifuge")) {
            TFMGGuiTextures.VAT_MACHINE.render(graphics, 55 - 12, 0);
            TFMGGuiTextures.CENTRIFUGE.render(graphics, 55 - 12, 32);
        }
        if (machines.contains("tfmg:electrode")) {
            TFMGGuiTextures.VAT_MACHINE.render(graphics, 55 - 12 - 32, 0);
            TFMGGuiTextures.VAT_MACHINE.render(graphics, 55 - 12 + 32, 0);
            TFMGGuiTextures.ELECTRODE.render(graphics, 55 - 3 - 32, 32);
            TFMGGuiTextures.ELECTRODE.render(graphics, 55 - 3 + 32, 32);
        }
        if (machines.contains("tfmg:graphite_electrode")) {
            TFMGGuiTextures.VAT_MACHINE.render(graphics, 55 - 12 - 32, 0);
            TFMGGuiTextures.VAT_MACHINE.render(graphics, 55 - 12 + 32, 0);
            TFMGGuiTextures.VAT_MACHINE.render(graphics, 55 - 12, 0);
            TFMGGuiTextures.GRAPHITE_ELECTRODE.render(graphics, 55 - 4 - 32, 32);
            TFMGGuiTextures.GRAPHITE_ELECTRODE.render(graphics, 55 - 4 + 32, 32);
            TFMGGuiTextures.GRAPHITE_ELECTRODE.render(graphics, 55 - 4, 32);
        }

    }

}
