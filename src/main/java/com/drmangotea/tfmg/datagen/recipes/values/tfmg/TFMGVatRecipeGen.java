package com.drmangotea.tfmg.datagen.recipes.values.tfmg;

import com.drmangotea.tfmg.TFMG;
import com.drmangotea.tfmg.base.pressure.Pressure;
import com.drmangotea.tfmg.content.machinery.vat.base.registry.operations.VatOperation;
import com.drmangotea.tfmg.content.machinery.vat.base.registry.types.VatType;
import com.drmangotea.tfmg.datagen.recipes.builder.VatRecipeGen;
import com.drmangotea.tfmg.registry.TFMGFluids;
import com.drmangotea.tfmg.registry.TFMGItems;
import com.drmangotea.tfmg.registry.TFMGTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import rbasamoyai.createbigcannons.index.CBCFluids;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.drmangotea.tfmg.datagen.recipes.TFMGRecipeProvider.F.*;
import static com.drmangotea.tfmg.datagen.recipes.TFMGRecipeProvider.I.*;

@SuppressWarnings("unused")
public class TFMGVatRecipeGen extends VatRecipeGen {
    public TFMGVatRecipeGen(PackOutput generator, CompletableFuture<HolderLookup.Provider> registries) {
        super(generator, registries, TFMG.MOD_ID);
    }


    GeneratedRecipe
            CONCRETE = create("concrete", b -> b
            .require(Blocks.SAND.asItem())
            .require(Blocks.GRAVEL.asItem())
            .require(TFMGItems.LIMESAND)
            .require(Fluids.WATER, 250)
            .output(TFMGFluids.LIQUID_CONCRETE.get(), 32000)
            .allowAllVatTypes()
            .mixing()
    ),

    WHITE_CONCRETE = create("white_concrete", b -> b
            .require(Items.WHITE_DYE)
            .require(TFMGFluids.LIQUID_CONCRETE.getSource(), 8000)
            .output(TFMGFluids.WHITE_LIQUID_CONCRETE.get(), 8000)
            .duration(100)
            .mixing()
    ),

    LIGHT_GRAY_CONCRETE = create("light_gray_concrete", b -> b
            .require(Items.LIGHT_GRAY_DYE)
            .require(TFMGFluids.LIQUID_CONCRETE.getSource(), 8000)
            .output(TFMGFluids.LIGHT_GRAY_LIQUID_CONCRETE.get(), 8000)
            .duration(100)
            .mixing()
    ),

    GRAY_CONCRETE = create("gray_concrete", b -> b
            .require(Items.GRAY_DYE)
            .require(TFMGFluids.LIQUID_CONCRETE.getSource(), 8000)
            .output(TFMGFluids.GRAY_LIQUID_CONCRETE.get(), 8000)
            .duration(100)
            .mixing()
    ),

    BLACK_CONCRETE = create("black_concrete", b -> b
            .require(Items.BLACK_DYE)
            .require(TFMGFluids.LIQUID_CONCRETE.getSource(), 8000)
            .output(TFMGFluids.BLACK_LIQUID_CONCRETE.get(), 8000)
            .duration(100)
            .mixing()
    ),

    BROWN_CONCRETE = create("brown_concrete", b -> b
            .require(Items.BROWN_DYE)
            .require(TFMGFluids.LIQUID_CONCRETE.getSource(), 8000)
            .output(TFMGFluids.BROWN_LIQUID_CONCRETE.get(), 8000)
            .duration(100)
            .mixing()
    ),

    RED_CONCRETE = create("red_concrete", b -> b
            .require(Items.RED_DYE)
            .require(TFMGFluids.LIQUID_CONCRETE.getSource(), 8000)
            .output(TFMGFluids.RED_LIQUID_CONCRETE.get(), 8000)
            .duration(100)
            .mixing()
    ),

    ORANGE_CONCRETE = create("orange_concrete", b -> b
            .require(Items.ORANGE_DYE)
            .require(TFMGFluids.LIQUID_CONCRETE.getSource(), 8000)
            .output(TFMGFluids.ORANGE_LIQUID_CONCRETE.get(), 8000)
            .duration(100)
            .mixing()
    ),

    YELLOW_CONCRETE = create("yellow_concrete", b -> b
            .require(Items.YELLOW_DYE)
            .require(TFMGFluids.LIQUID_CONCRETE.getSource(), 8000)
            .output(TFMGFluids.YELLOW_LIQUID_CONCRETE.get(), 8000)
            .duration(100)
            .mixing()
    ),

    LIME_CONCRETE = create("lime_concrete", b -> b
            .require(Items.LIME_DYE)
            .require(TFMGFluids.LIQUID_CONCRETE.getSource(), 8000)
            .output(TFMGFluids.LIME_LIQUID_CONCRETE.get(), 8000)
            .duration(100)
            .mixing()
    ),

    GREEN_CONCRETE = create("green_concrete", b -> b
            .require(Items.GREEN_DYE)
            .require(TFMGFluids.LIQUID_CONCRETE.getSource(), 8000)
            .output(TFMGFluids.GREEN_LIQUID_CONCRETE.get(), 8000)
            .duration(100)
            .mixing()
    ),

    CYAN_CONCRETE = create("cyan_concrete", b -> b
            .require(Items.CYAN_DYE)
            .require(TFMGFluids.LIQUID_CONCRETE.getSource(), 8000)
            .output(TFMGFluids.CYAN_LIQUID_CONCRETE.get(), 8000)
            .duration(100)
            .mixing()
    ),

    LIGHT_BLUE_CONCRETE = create("light_blue_concrete", b -> b
            .require(Items.LIGHT_BLUE_DYE)
            .require(TFMGFluids.LIQUID_CONCRETE.getSource(), 8000)
            .output(TFMGFluids.LIGHT_BLUE_LIQUID_CONCRETE.get(), 8000)
            .duration(100)
            .mixing()
    ),

    BLUE_CONCRETE = create("blue_concrete", b -> b
            .require(Items.BLUE_DYE)
            .require(TFMGFluids.LIQUID_CONCRETE.getSource(), 8000)
            .output(TFMGFluids.BLUE_LIQUID_CONCRETE.get(), 8000)
            .duration(100)
            .mixing()
    ),

    PURPLE_CONCRETE = create("purple_concrete", b -> b
            .require(Items.PURPLE_DYE)
            .require(TFMGFluids.LIQUID_CONCRETE.getSource(), 8000)
            .output(TFMGFluids.PURPLE_LIQUID_CONCRETE.get(), 8000)
            .duration(100)
            .mixing()
    ),

    MAGENTA_CONCRETE = create("magenta_concrete", b -> b
            .require(Items.MAGENTA_DYE)
            .require(TFMGFluids.LIQUID_CONCRETE.getSource(), 8000)
            .output(TFMGFluids.MAGENTA_LIQUID_CONCRETE.get(), 8000)
            .duration(100)
            .mixing()
    ),

    PINK_CONCRETE = create("pink_concrete", b -> b
            .require(Items.PINK_DYE)
            .require(TFMGFluids.LIQUID_CONCRETE.getSource(), 8000)
            .output(TFMGFluids.PINK_LIQUID_CONCRETE.get(), 8000)
            .duration(100)
            .mixing()
    ),

    ARC_FURNACE_STEEL = create("arc_furnace_steel", b -> b
            .require(crushedRawIron())
            .require(TFMGTags.Items.FLUX.tag)
            .require(TFMGItems.COAL_COKE_DUST)
            .output(0.9f, TFMGItems.COAL_COKE_DUST)
            .output(TFMGFluids.MOLTEN_STEEL.get(), 90)
            .output(TFMGFluids.MOLTEN_SLAG.get(), 160)
            .duration(20)
            .allowFireproof()
            .minSize(9)
            .arcBlasting()
    ),

    NEON = create("neon", b -> b
            .require(TFMGFluids.AIR.get(), 1000)
            .output(TFMGFluids.NEON.get(), 1)
            .duration(10)
            .centrifuge()
            .allowAllVatTypes()
    ),

    SULFURIC_ACID = create("sulfuric_acid", b -> b
            .require(SizedFluidIngredient.of(water(), 1000))
            .require(sulfurDust())
            .require(sulfurDust())
            .require(sulfurDust())
            .require(nitrateDust())
            .output(sulfuricAcid(), 500)
            .mixing()
            .duration(5)
            .allowAllVatTypes()
    ),

    RUBBER = create("rubber", b -> b
            .require(SizedFluidIngredient.of(heavyOil(), 250))
            .require(sulfurDust())
            .output(rubber())
            .mixing()
            .allowAllVatTypes()
            .duration(40)
            .heatLevel(2)
    ),

    NAPHTHA = create("naphtha", b -> b
            .require(SizedFluidIngredient.of(naphtha(), 500))
            .output(ethylene(), 250)
            .output(propylene(), 250)
            .mixing()
            .duration(20)
            .allowAllVatTypes()
            .heatLevel(2)
    ),

    PLASTIC_FROM_ETHYLENE = create("plastic_from_ethylene", b -> b
            .require(SizedFluidIngredient.of(ethylene(), 500))
            .output(liquidPlastic(), 500)
            .mixing()
            .allowAllVatTypes()
            .duration(30)
            .heatLevel(2)
    ),

    PLASTIC_FROM_PROPYLENE = create("plastic_from_propylene", b -> b
            .require(SizedFluidIngredient.of(propylene(), 500))
            .output(liquidPlastic(), 500)
            .mixing()
            .duration(30)
            .allowAllVatTypes()
            .heatLevel(2)
    ),

    ETCHED_CIRCUIT_BOARD = create("etched_circuit_board", b -> b
            .require(TFMGItems.COATED_CIRCUIT_BOARD)
            .require(TFMGFluids.SULFURIC_ACID.getSource(), 250)
            .output(TFMGItems.ETCHED_CIRCUIT_BOARD)
            .duration(100)
            .mixing()
            .allowAllVatTypes()
    ),

    ALUMINUM = create("aluminum", b -> b
            .require(TFMGItems.BAUXITE_POWDER)
            .require(TFMGItems.BAUXITE_POWDER)
            .require(TFMGItems.BAUXITE_POWDER)
            .require(TFMGItems.BAUXITE_POWDER)
            .output(TFMGItems.ALUMINUM_INGOT)
            .output(.5f, TFMGItems.ALUMINUM_NUGGET, 4)
            .output(.25f, TFMGItems.ALUMINUM_NUGGET, 2)
            .output(TFMGFluids.CARBON_DIOXIDE.get(), 500)
            .duration(100)
            .electrolysis()
            .allowNonCastIron()
            .heatLevel(2)
    ),

    //CBC
    Nethersteel = create("nethersteel", b -> b.whenModLoaded("createbigcannons")
            .require(Items.NETHERITE_SCRAP)
            .require(TFMGTags.Fluids.MOLTEN_STEEL.tag, 360)
            .output(CBCFluids.MOLTEN_NETHERSTEEL.get(), 360)
            .arcBlasting()
            .allowFireproof()
            .heatLevel(2)
    );

    public static class VatRecipeValues {
        public List<VatOperation> machines;
        public int minSize;
        public int heat;
        public Pressure pressure;
        public List<VatType> allowedVatTypes;

        public VatRecipeValues() {
            machines = new ArrayList<>();
            minSize = 1;
            heat = 0;
            pressure = Pressure.EMPTY;
            allowedVatTypes = new ArrayList<>();
            allowedVatTypes.add(new VatType(TFMG.asResource("steel")));
            allowedVatTypes.add(new VatType(TFMG.asResource("cast_iron")));
            allowedVatTypes.add(new VatType(TFMG.asResource("fireproof")));
        }

        public VatRecipeValues heat(int heat) {
            this.heat = heat;
            return this;
        }

        public VatRecipeValues pressure(int kpa) {
            this.pressure = Pressure.of(kpa);
            return this;
        }

    }

    @Override @Nonnull
    public String getName() {
        return "TFMG'S Vat Recipes";
    }
}