package com.drmangotea.tfmg.recipes;

import com.drmangotea.tfmg.TFMG;
import com.drmangotea.tfmg.base.pressure.Pressure;
import com.drmangotea.tfmg.content.machinery.vat.base.registry.VatOperation;
import com.drmangotea.tfmg.registry.TFMGRecipeTypes;
import com.drmangotea.tfmg.registry.TFMGVatOperations;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;


import java.util.List;

public class VatMachineRecipe extends ProcessingRecipe<RecipeInput, VatRecipeParams> {
    public List<VatOperation> machines;
    public List<ResourceLocation> allowedVatTypes;
    public int minSize;
    public int heatLevel;
    public Pressure pressure;

    public VatMachineRecipe(VatRecipeParams params) {
        super(TFMGRecipeTypes.VAT_MACHINE_RECIPE, params);
        machines = params.machines;
        allowedVatTypes = params.allowedVatTypes;
        minSize = params.min_size;
        heatLevel = params.heat_level;
        pressure = params.pressure;
    }

    @Override
    protected int getMaxInputCount() {
        return 4;
    }
    @Override
    protected int getMaxOutputCount() {
        return 4;
    }
    @Override
    protected int getMaxFluidInputCount() {
        return 4;
    }
    @Override
    protected int getMaxFluidOutputCount() {
        return 4;
    }


    @Override
    public boolean matches(RecipeInput inv, Level worldIn) {
        return false;
    }

    @Override
    protected boolean canSpecifyDuration() {
        return true;
    }

    @Override
    protected boolean canRequireHeat() {
        return true;
    }

    @FunctionalInterface
    public interface Factory<R extends VatMachineRecipe> extends ProcessingRecipe.Factory<VatRecipeParams, R> {
        R create(VatRecipeParams params);
    }

    public static class Builder<R extends VatMachineRecipe> extends ProcessingRecipeBuilder<VatRecipeParams, R, VatMachineRecipe.Builder<R>> {
        public Builder(VatMachineRecipe.Factory<R> factory, ResourceLocation recipeId) {
            super(factory, recipeId);
        }

        @Override
        protected VatRecipeParams createParams() {
            return new VatRecipeParams();
        }

        @Override
        public VatMachineRecipe.Builder<R> self() {
            return this;
        }

        public VatMachineRecipe.Builder<R> pressure(int kpa) {
            params.pressure = Pressure.of(kpa);
            return this;
        }

        public VatMachineRecipe.Builder<R> heatLevel(int level) {
            if (level < 0) {
                throw new IllegalArgumentException("Heat level can not be less than 0!");
            }
            params.heat_level = level;
            return this;
        }

        public VatMachineRecipe.Builder<R> operation(VatOperation vatOperationEntry) {
            params.machines.add(vatOperationEntry);
            return this;
        }

        public VatMachineRecipe.Builder<R> operation(List<VatOperation> vatOperationEntry) {
            params.machines.addAll(vatOperationEntry);
            return this;
        }

        public VatMachineRecipe.Builder<R> arcBlasting() {
            params.machines.add(TFMGVatOperations.GRAPHITE_ELECTRODE.get());
            params.machines.add(TFMGVatOperations.GRAPHITE_ELECTRODE.get());
            params.machines.add(TFMGVatOperations.GRAPHITE_ELECTRODE.get());
            return this;
        }

        public VatMachineRecipe.Builder<R> centrifuge() {
            params.machines.add(TFMGVatOperations.CENTRIFUGE.get());
            return this;
        }

        public VatMachineRecipe.Builder<R> mixing() {
            params.machines.add(TFMGVatOperations.MIXING.get());
            return this;
        }

        public VatMachineRecipe.Builder<R> electrolysis() {
            params.machines.add(TFMGVatOperations.ELECTRODE.get());
            params.machines.add(TFMGVatOperations.ELECTRODE.get());
            return this;
        }

        public VatMachineRecipe.Builder<R> freezing() {
            params.machines.add(TFMGVatOperations.FREEZING.get());
            return this;
        }

        public VatMachineRecipe.Builder<R> intenseFreezing() {
            return this.freezing().freezing().freezing();
        }

        public VatMachineRecipe.Builder<R> minSize(int minSize) {
            params.min_size = minSize;
            return this;
        }

        public VatMachineRecipe.Builder<R> allowAllVatTypes() {
            params.allowedVatTypes.add(TFMG.asResource("cast_iron_vat"));
            params.allowedVatTypes.add(TFMG.asResource("steel_vat"));
            params.allowedVatTypes.add(TFMG.asResource("firebrick_lined_vat"));
            return this;
        }

        public VatMachineRecipe.Builder<R> allowNonCastIron() {
            params.allowedVatTypes.add(TFMG.asResource("steel_vat"));
            params.allowedVatTypes.add(TFMG.asResource("firebrick_lined_vat"));
            return this;
        }

        public VatMachineRecipe.Builder<R> allowSteelVat() {
            params.allowedVatTypes.add(TFMG.asResource("steel_vat"));
            return this;
        }

        public VatMachineRecipe.Builder<R> allowCastIronVat() {
            params.allowedVatTypes.add(TFMG.asResource("cast_iron_vat"));
            return this;
        }

        public VatMachineRecipe.Builder<R> allowFirebrickLinedVat() {
            params.allowedVatTypes.add(TFMG.asResource("firebrick_lined_vat"));
            return this;
        }

        public VatMachineRecipe.Builder<R> allowTypes(List<ResourceLocation> types) {
            params.allowedVatTypes.addAll(types);
            return this;
        }

        public VatMachineRecipe.Builder<R> allowType(ResourceLocation type) {
            params.allowedVatTypes.add(type);
            return this;
        }
    }

    public static class Serializer<R extends VatMachineRecipe> implements RecipeSerializer<R> {
        private final MapCodec<R> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, R> streamCodec;

        public Serializer(ProcessingRecipe.Factory<VatRecipeParams, R> factory) {
            this.codec = ProcessingRecipe.codec(factory, VatRecipeParams.CODEC);
            this.streamCodec = ProcessingRecipe.streamCodec(factory, VatRecipeParams.STREAM_CODEC);
        }

        @Override
        public MapCodec<R> codec() {
            return codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, R> streamCodec() {
            return streamCodec;
        }
    }
}
