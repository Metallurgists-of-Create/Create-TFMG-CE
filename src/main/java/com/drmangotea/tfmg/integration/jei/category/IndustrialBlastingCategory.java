package com.drmangotea.tfmg.integration.jei.category;


import com.drmangotea.tfmg.base.lang.TFMGLang;
import com.drmangotea.tfmg.base.lang.TFMGTexts;
import com.drmangotea.tfmg.recipes.IndustrialBlastingRecipe;
import com.drmangotea.tfmg.integration.jei.render.BlastFurnace;
import com.drmangotea.tfmg.registry.TFMGFluids;
import com.drmangotea.tfmg.registry.TFMGItems;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class IndustrialBlastingCategory extends CreateRecipeCategory<IndustrialBlastingRecipe> {

    private final BlastFurnace blastFurnace = new BlastFurnace();

    public IndustrialBlastingCategory(Info<IndustrialBlastingRecipe> info) {
        super(info);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, IndustrialBlastingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 25, 13)
                .setBackground(getRenderedSlot(), -1, -1)
                .addIngredients(recipe.getIngredients().get(0));
        if (recipe.getIngredients().size() > 1) {
            builder.addSlot(RecipeIngredientRole.INPUT, 5, 13)
                    .setBackground(getRenderedSlot(), -1, -1)
                    .addIngredients(recipe.getIngredients().get(1));
        }
        builder.addSlot(RecipeIngredientRole.INPUT, 70, 13)
                .setBackground(getRenderedSlot(), -1, -1)
                .addItemStack(new ItemStack(TFMGItems.COAL_COKE_DUST.get()));

        //fluid
        addFluidSlot(builder, 140, 117, recipe.getFluidResults().get(0));
        if (recipe.getFluidResults().size() > 2)
            addFluidSlot(builder, 160, 117, recipe.getFluidResults().get(1));

        if (recipe.hotAirUsage > 0) {
            addFluidSlot(builder, 90, 13, SizedFluidIngredient.of(new FluidStack(FluidHelper.convertToStill(TFMGFluids.HOT_AIR.get()), recipe.hotAirUsage * recipe.getProcessingDuration()))).addRichTooltipCallback((slotView, tooltip) -> tooltip.add(TFMGLang.translate("recipe.over_time", StringUtil.formatTickDuration(recipe.getProcessingDuration(), 1)).component()));
        }
    }

    @Override
    public void draw(IndustrialBlastingRecipe recipe, IRecipeSlotsView iRecipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        blastFurnace.draw(graphics, 50, 135);

        AllGuiTextures.JEI_ARROW.render(graphics, 96, 121);
        AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 45, 15);
    }

}
