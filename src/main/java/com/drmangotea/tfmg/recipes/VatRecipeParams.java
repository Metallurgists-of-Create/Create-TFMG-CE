package com.drmangotea.tfmg.recipes;

import com.drmangotea.tfmg.TFMG;
import com.drmangotea.tfmg.base.pressure.Pressure;
import com.drmangotea.tfmg.content.machinery.vat.base.registry.VatOperation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class VatRecipeParams extends ProcessingRecipeParams {
    public static final List<ResourceLocation> types = new ArrayList<>();

    static {
        types.add(TFMG.asResource("steel_vat"));
        types.add(TFMG.asResource("cast_iron_vat"));
        types.add(TFMG.asResource("firebrick_lined_vat"));
    }

    public static final MapCodec<VatRecipeParams> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            codec(VatRecipeParams::new).forGetter(Function.identity()),
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("min_size", 0).forGetter(VatRecipeParams::getMinSize),
            Codec.INT.optionalFieldOf("heat_level", 0).forGetter(VatRecipeParams::getHeatLevel),
            Pressure.CODEC.optionalFieldOf("pressure", Pressure.EMPTY).forGetter(VatRecipeParams::getPressure),
            VatOperation.CODEC.listOf().optionalFieldOf("machines", new ArrayList<>()).forGetter(VatRecipeParams::getMachines),
            ResourceLocation.CODEC.listOf().optionalFieldOf("allowed_vat_types", types).forGetter(VatRecipeParams::getAllowedVatTypes)
    ).apply(instance, (params, min_size, heat_level,pressure, machines, allowed_vat_types) -> {
        params.machines = machines;
        params.min_size = min_size;
        params.heat_level = heat_level;
        params.pressure = pressure;
        params.allowedVatTypes = allowed_vat_types;
        return params;
    }));

    public static final StreamCodec<RegistryFriendlyByteBuf, VatRecipeParams> STREAM_CODEC = streamCodec(VatRecipeParams::new);

    public int min_size = 1;
    public int heat_level = 0;
    public Pressure pressure = Pressure.EMPTY;

    public List<VatOperation> machines = new ArrayList<>();
    public List<ResourceLocation> allowedVatTypes = new ArrayList<>();

    protected final int getHeatLevel() {
        return heat_level;
    }

    protected final Pressure getPressure() {
        return pressure;
    }

    protected final int getMinSize() {
        return min_size;
    }

    protected final List<VatOperation> getMachines() {
        return machines;
    }

    protected final List<ResourceLocation> getAllowedVatTypes() {
        return allowedVatTypes;
    }

    @Override
    protected void encode(RegistryFriendlyByteBuf buffer) {
        super.encode(buffer);
        ByteBufCodecs.INT.encode(buffer, min_size);
        ByteBufCodecs.INT.encode(buffer, heat_level);
        Pressure.STREAM_CODEC.encode(buffer, pressure);

        CatnipStreamCodecBuilders.list(VatOperation.STREAM_CODEC).encode(buffer, machines);
        CatnipStreamCodecBuilders.list(ResourceLocation.STREAM_CODEC).encode(buffer, allowedVatTypes);

    }

    @Override
    protected void decode(RegistryFriendlyByteBuf buffer) {
        super.decode(buffer);
        min_size = ByteBufCodecs.INT.decode(buffer);
        heat_level = ByteBufCodecs.INT.decode(buffer);
        pressure = Pressure.STREAM_CODEC.decode(buffer);

        machines = CatnipStreamCodecBuilders.list(VatOperation.STREAM_CODEC).decode(buffer);
        allowedVatTypes = CatnipStreamCodecBuilders.list(ResourceLocation.STREAM_CODEC).decode(buffer);
    }
}
