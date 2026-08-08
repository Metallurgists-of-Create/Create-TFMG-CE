package com.drmangotea.tfmg.recipes.jei;

import com.drmangotea.tfmg.recipes.VatMachineRecipe;
import com.drmangotea.tfmg.registry.TFMGGuiTextures;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.item.ItemHelper;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.createmod.catnip.data.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.apache.commons.lang3.mutable.MutableInt;

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

        TFMGGuiTextures.VAT_BAROMETER.render(graphics, 128, 0);
        if (recipe.pressure==0) {
            TFMGGuiTextures.VAT_BAROMETER_NEEDLE_OFF.render(graphics,  128+20-13, 0+24);
        } else {

            TFMGGuiTextures i;

            if (recipe.pressure == -9) { i = TFMGGuiTextures.VAT_BAROMETER_NEEDLE_LOWNINE; }
            else if (recipe.pressure == 9) {i = TFMGGuiTextures.VAT_BAROMETER_NEEDLE_HIGHNINE; }
            else if (recipe.pressure == -8) { i = TFMGGuiTextures.VAT_BAROMETER_NEEDLE_LOWEIGHT; }
            else if (recipe.pressure == 8) { i = TFMGGuiTextures.VAT_BAROMETER_NEEDLE_HIGHEIGHT; }
            else if (recipe.pressure == -7) { i = TFMGGuiTextures.VAT_BAROMETER_NEEDLE_LOWSEVEN; }
            else if (recipe.pressure == 7) { i = TFMGGuiTextures.VAT_BAROMETER_NEEDLE_HIGHSEVEN; }
            else if (recipe.pressure == -6) { i = TFMGGuiTextures.VAT_BAROMETER_NEEDLE_LOWSIX; }
            else if (recipe.pressure == 6) { i = TFMGGuiTextures.VAT_BAROMETER_NEEDLE_HIGHSIX; }
            else if (recipe.pressure == -5) { i = TFMGGuiTextures.VAT_BAROMETER_NEEDLE_LOWFIVE; }
            else if (recipe.pressure == 5) { i = TFMGGuiTextures.VAT_BAROMETER_NEEDLE_HIGHFIVE; }
            else if (recipe.pressure == -4) { i = TFMGGuiTextures.VAT_BAROMETER_NEEDLE_LOWFOUR; }
            else if (recipe.pressure == 4) { i = TFMGGuiTextures.VAT_BAROMETER_NEEDLE_HIGHFOUR; }
            else if (recipe.pressure == -3) { i = TFMGGuiTextures.VAT_BAROMETER_NEEDLE_LOWTHREE; }
            else if (recipe.pressure == 3) { i = TFMGGuiTextures.VAT_BAROMETER_NEEDLE_HIGHTHREE; }
            else if (recipe.pressure == -2) { i = TFMGGuiTextures.VAT_BAROMETER_NEEDLE_LOWTWO; }
            else if (recipe.pressure == 2) { i = TFMGGuiTextures.VAT_BAROMETER_NEEDLE_HIGHTWO; }
            else if (recipe.pressure == -1) { i = TFMGGuiTextures.VAT_BAROMETER_NEEDLE_LOWONE; }
            else { i = TFMGGuiTextures.VAT_BAROMETER_NEEDLE_HIGHONE; }
            if (recipe.pressure<0) { i.render(graphics, 128 +20 - (i.width-2), 0 + 24-(i.height-2)); }
            else {i.render(graphics, 128 + 20 , 0 + 24-(i.height-2));}


        }
        if(recipe.heatLevel!=0) {
            if (recipe.heatLevel>9) {
                for (int i = 9; i > 0; i--) {
                    if (recipe.heatLevel >= 9 + i) {
                        TFMGGuiTextures.VAT_SUPERHEATER.render(graphics, i * 10, 109);
                    } else {
                        TFMGGuiTextures.VAT_HEATER.render(graphics, i * 10, 109);
                    }
                }

            }else if (recipe.heatLevel>0) {
                for (int i = recipe.heatLevel; i > 0; i--) {
                    TFMGGuiTextures.VAT_HEATER.render(graphics, i * 10, 109);
                }
            } else {
                for (int i = -1 * recipe.heatLevel; i > 0; i--) {
                    TFMGGuiTextures.VAT_FREEZER.render(graphics, i * 10, 109);
                }
            }

            //graphics.drawString(Minecraft.getInstance().font, String.valueOf((recipe.heatLevel + 10f) / 10f), 76.0F, 113.0F, 0xFF501C, false);
        }

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


        //AllGuiTextures.JEI_ARROW.render(graphics, 85, 32);
        //AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 43, 4);


    }

    private void renderHeated(HeatCondition heatCondition, GuiGraphics graphics) {
        if (heatCondition == HeatCondition.HEATED)
            TFMGGuiTextures.VAT_HEATER.render(graphics, 55 - 10, 109);
        if (heatCondition == HeatCondition.SUPERHEATED)
            TFMGGuiTextures.VAT_SUPERHEATER.render(graphics, 55 - 10, 109);
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