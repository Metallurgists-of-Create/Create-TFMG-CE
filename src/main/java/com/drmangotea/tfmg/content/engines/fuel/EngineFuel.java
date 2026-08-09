package com.drmangotea.tfmg.content.engines.fuel;

import com.drmangotea.tfmg.TFMGRegistries;
import com.drmangotea.tfmg.registry.TFMGEngineFuelTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record EngineFuel(@Nullable ResourceKey<EngineFuelType> fuelType) {

    public static final Codec<EngineFuel> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(TFMGRegistries.ENGINE_FUEL_TYPE).fieldOf("fuel_type").forGetter(EngineFuel::fuelType)
    ).apply(instance, EngineFuel::new));

    public static final StreamCodec<ByteBuf, EngineFuel> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(TFMGRegistries.ENGINE_FUEL_TYPE),
            EngineFuel::fuelType,
            EngineFuel::new
    );

    public static final EngineFuel EMPTY = new EngineFuel(TFMGEngineFuelTypes.FALLBACK);

    public static EngineFuel createForType(RegistryAccess registryAccess, Fluid fluid) {
        Optional<Holder.Reference<EngineFuelType>> type = EngineFuelType.getTypeForFluid(registryAccess, fluid);
        return type.map(typeReference -> new EngineFuel(typeReference.getKey())).orElse(EMPTY);
    }

    public static EngineFuel createForType(RegistryAccess registryAccess, FluidStack stack) {
        return createForType(registryAccess, stack.getFluid());
    }

    public boolean hasFuel() {
        return fuelType != TFMGEngineFuelTypes.FALLBACK;
    }

    public Optional<EngineFuelType> getFuelType(RegistryAccess registryAccess) {
        return registryAccess.registryOrThrow(TFMGRegistries.ENGINE_FUEL_TYPE).getOptional(fuelType);
    }

    public EngineFuelType getFuelTypeOrThrow(RegistryAccess registryAccess) {
        return getFuelType(registryAccess).orElseThrow(() -> new IllegalStateException("No engine fuel type found for " + fuelType));
    }
}
