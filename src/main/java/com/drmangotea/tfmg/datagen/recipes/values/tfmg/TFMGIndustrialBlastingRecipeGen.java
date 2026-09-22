package com.drmangotea.tfmg.datagen.recipes.values.tfmg;

import com.drmangotea.tfmg.TFMG;
import com.drmangotea.tfmg.datagen.recipes.builder.IndustrialBlastingRecipeGen;
import com.drmangotea.tfmg.registry.TFMGFluids;
import com.drmangotea.tfmg.registry.TFMGTags;
import com.simibubi.create.AllItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

import static com.drmangotea.tfmg.datagen.recipes.TFMGRecipeProvider.I.ironDust;

@SuppressWarnings("unused")
public class TFMGIndustrialBlastingRecipeGen extends IndustrialBlastingRecipeGen {
    GeneratedRecipe

    SILICON = create("silicon", b -> b
            .require(Items.QUARTZ)
            .output(TFMGFluids.LIQUID_SILICON.get(),40)
            .duration(5)
    ),

    STEEL_CRUSHED = create("steel", b -> b
            .require(AllItems.CRUSHED_IRON)
            .require(TFMGTags.Items.FLUX.tag)
            .output(TFMGFluids.MOLTEN_STEEL.get(),90)
            .output(TFMGFluids.MOLTEN_SLAG.get(),80)
            .output(TFMGFluids.FURNACE_GAS.get(),200)
            .duration(20)
            .hotAirUsage(20)
    ),
    STEEL_RAW = create("steel_from_raw_iron", b -> b
            .require(Items.RAW_IRON)
            .require(TFMGTags.Items.FLUX.tag)
            .output(TFMGFluids.MOLTEN_STEEL.get(),180)
            .output(TFMGFluids.MOLTEN_SLAG.get(),160)
            .output(TFMGFluids.FURNACE_GAS.get(),200)
            .duration(40)
            .hotAirUsage(40)
    ),
    STEEL_DUST = create("steel_from_dust", b -> b
            .require(ironDust())
            .require(TFMGTags.Items.FLUX.tag)
            .output(TFMGFluids.MOLTEN_STEEL.get(),90)
            .output(TFMGFluids.MOLTEN_SLAG.get(),80)
            .output(TFMGFluids.FURNACE_GAS.get(),20)
            .duration(20)
            .hotAirUsage(20)
    )



    ;

    public TFMGIndustrialBlastingRecipeGen(PackOutput generator, CompletableFuture<HolderLookup.Provider> registries) {
        super(generator, registries, TFMG.MOD_ID);
    }

    @Override @Nonnull
    public String getName() {
        return "TFMG'S Industrial Blasting Recipes";
    }

}
