package com.drmangotea.tfmg.datagen.integration;

import com.drmangotea.tfmg.TFMG;
import com.drmangotea.tfmg.registry.*;
import dev.metallurgists.rutile.Rutile;
import dev.metallurgists.rutile.api.data.provider.composition.FluidCompositionProvider;
import dev.metallurgists.rutile.api.data.provider.composition.ItemCompositionProvider;
import dev.metallurgists.rutile.registry.RutileElements;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TFMGRutileProvider {

    public static class Item extends ItemCompositionProvider {
        public Item(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(TFMG.MOD_ID, output, registries);
        }

        @Override
        public void generate(HolderLookup.Provider registries) {
            addData(List.of(TFMGBlocks.SULFUR.asItem(), TFMGItems.SULFUR_DUST.asItem()), c -> c.element(RutileElements.S));
            addData(List.of(TFMGBlocks.RAW_NICKEL_BLOCK.asItem(), TFMGBlocks.NICKEL_BLOCK.asItem(), TFMGItems.RAW_NICKEL.asItem(), TFMGItems.NICKEL_INGOT.asItem(), TFMGItems.NICKEL_NUGGET.asItem(), TFMGItems.NICKEL_SHEET.asItem()), c -> c.element(RutileElements.Ni));
            addData(List.of(TFMGBlocks.RAW_LEAD_BLOCK.asItem(), TFMGBlocks.LEAD_BLOCK.asItem(), TFMGItems.RAW_LEAD.asItem(), TFMGItems.LEAD_INGOT.asItem(), TFMGItems.LEAD_NUGGET.asItem(), TFMGItems.LEAD_SHEET.asItem()), c -> c.element(RutileElements.Pb));
            addData(List.of(TFMGBlocks.RAW_LITHIUM_BLOCK.asItem(), TFMGBlocks.LITHIUM_BLOCK.asItem(), TFMGItems.RAW_LITHIUM.asItem(), TFMGItems.LITHIUM_INGOT.asItem(), TFMGItems.LITHIUM_NUGGET.asItem(), TFMGItems.CRUSHED_LITHIUM.asItem()), c -> c.element(RutileElements.Li));
            addData(List.of(TFMGBlocks.STEEL_BLOCK.asItem(), TFMGItems.STEEL_INGOT.asItem(), TFMGItems.STEEL_NUGGET.asItem(), TFMGItems.HEAVY_PLATE.asItem()), c -> c.element(RutileElements.Fe));
            addData(List.of(TFMGBlocks.CAST_IRON_BLOCK.asItem(), TFMGItems.CAST_IRON_INGOT.asItem(), TFMGItems.CAST_IRON_NUGGET.asItem(), TFMGItems.CAST_IRON_SHEET.asItem()), c -> c.element(RutileElements.Fe));
            addData(List.of(TFMGBlocks.ALUMINUM_BLOCK.asItem(), TFMGItems.ALUMINUM_INGOT.asItem(), TFMGItems.ALUMINUM_NUGGET.asItem(), TFMGItems.ALUMINUM_SHEET.asItem()), c -> c.element(RutileElements.Al));
            addData(List.of(TFMGBlocks.CONSTANTAN_BLOCK.asItem(), TFMGItems.CONSTANTAN_INGOT.asItem(), TFMGItems.CONSTANTAN_NUGGET.asItem()), c -> c.element(RutileElements.Cu).element(RutileElements.Ni));
            addData(List.of(TFMGBlocks.COAL_COKE_BLOCK.asItem(), TFMGItems.COAL_COKE.asItem(), TFMGItems.COAL_COKE_DUST.asItem()), c -> c.element(RutileElements.C));
            addData(List.of(TFMGBlocks.LAMINATED_MAGNETIC_ALLOY_BLOCK.asItem(), TFMGItems.MAGNETIC_ALLOY_INGOT.asItem(), TFMGItems.MAGNETIC_ALLOY_SHEET.asItem(), TFMGItems.MAGNET.asItem()), c -> c.element(RutileElements.Ni, 2).element(RutileElements.Si).element(RutileElements.Fe, 2));
            addData(List.of(TFMGItems.LIMESAND.asItem()), c -> c.element(RutileElements.Ca).element(RutileElements.C).element(RutileElements.O, 3));
            addData(List.of(TFMGItems.NITRATE_DUST.asItem()), c -> c.element(RutileElements.K).element(RutileElements.N).element(RutileElements.O, 3));
            addData(List.of(TFMGItems.SILICON_INGOT.asItem()), c -> c.element(RutileElements.Si));
            addData(List.of(TFMGItems.COPPER_SULFATE.asItem()), c -> c.element(RutileElements.Cu).element(RutileElements.S).element(RutileElements.O, 4));
            addData(List.of(TFMGItems.COPPER_ELECTRODE.asItem()), c -> c.element(RutileElements.Cu));
            addData(List.of(TFMGItems.ZINC_ELECTRODE.asItem()), c -> c.element(RutileElements.Zn));
            addData(List.of(TFMGItems.GRAPHITE_ELECTRODE.asItem()), c -> c.element(RutileElements.C));
            addData(List.of(TFMGPaletteStoneTypes.BAUXITE.getBaseBlock().get().asItem(), TFMGItems.BAUXITE_POWDER.asItem()), c -> c.element(RutileElements.Al, 2).element(RutileElements.O, 3).element(RutileElements.H, 2));
            addData(List.of(TFMGPaletteStoneTypes.GALENA.getBaseBlock().get().asItem()), c -> c.element(RutileElements.Pb).element(RutileElements.S));
        }
    }

    public static class Fluid extends FluidCompositionProvider {
        public Fluid(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(Rutile.ID, output, registries);
        }

        @Override
        public void generate(HolderLookup.Provider registries) {
            addData(TFMGFluids.LPG.get().getSource(), c -> c
                    .element(RutileElements.C, 5)
                    .element(RutileElements.H, 12));
            addData(TFMGFluids.BUTANE.get().getSource(), c -> c
                    .element(RutileElements.C, 4)
                    .element(RutileElements.H, 10));
            addData(TFMGFluids.PROPANE.get().getSource(), c -> c
                    .element(RutileElements.C, 3)
                    .element(RutileElements.H, 8));
            addData(TFMGFluids.HYDROGEN.get().getSource(), c -> c.element(RutileElements.H));
            addData(TFMGFluids.ETHYLENE.get().getSource(), c -> c
                    .element(RutileElements.C, 2)
                    .element(RutileElements.H, 4));
            addData(TFMGFluids.PROPYLENE.get().getSource(), c -> c
                    .element(RutileElements.C, 3)
                    .element(RutileElements.H, 6));
            addData(TFMGFluids.NEON.get().getSource(), c -> c.element(RutileElements.Ne));
            addData(TFMGFluids.CARBON_DIOXIDE.get().getSource(), c -> c
                    .element(RutileElements.C)
                    .element(RutileElements.O, 2));
            addData(TFMGFluids.GASOLINE.get().getSource(), c -> c
                    .element(RutileElements.C, 12)
                    .element(RutileElements.H, 2));
            addData(TFMGFluids.DIESEL.get().getSource(), c -> c
                    .element(RutileElements.C, 12)
                    .element(RutileElements.H, 23));
            addData(TFMGFluids.NAPHTHA.get().getSource(), c -> c
                    .element(RutileElements.C, 5)
                    .element(RutileElements.H, 10));
            addData(TFMGFluids.KEROSENE.get().getSource(), c -> c
                    .element(RutileElements.C, 12)
                    .element(RutileElements.H, 26));
            addData(TFMGFluids.CREOSOTE.get().getSource(), c -> c
                    .element(RutileElements.C)
                    .element(RutileElements.H)
                    .setAmount(2).next()
                    .element(RutileElements.O));
            addData(TFMGFluids.MOLTEN_STEEL.get().getSource(), c -> c.element(RutileElements.Fe));
            addData(TFMGFluids.MOLTEN_SLAG.get().getSource(), c -> c
                    .element(RutileElements.Si)
                    .element(RutileElements.O, 2)
                    .setAmount(13).next()
                    .element(RutileElements.Fe)
                    .element(RutileElements.O)
                    .setAmount(20).next()
                    .element(RutileElements.Ca)
                    .element(RutileElements.O)
                    .setAmount(40)
            );

            addData(TFMGFluids.LIQUID_SILICON.get().getSource(), c -> c.element(RutileElements.Si));
            addData(TFMGFluids.NAPALM.get().getSource(), c -> c
                    .element(RutileElements.C, 12)
                    .element(RutileElements.H, 5)
                    .next()
                    .element(RutileElements.Al));
            addData(TFMGFluids.SULFURIC_ACID.get().getSource(), c -> c
                    .element(RutileElements.H, 2)
                    .element(RutileElements.S)
                    .element(RutileElements.O, 4));
        }
    }
}
