package com.drmangotea.tfmg.content.engines.fuels;

import com.drmangotea.tfmg.TFMGRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.foundation.fluid.FluidHelper;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;

public record EngineFuelType(HolderSet<Fluid> fluids, float speed, float efficiency, float torque) {
    public static final Map<Fluid, Optional<Holder.Reference<EngineFuelType>>> typeCache = new HashMap<>();

    public static final Codec<EngineFuelType> CODEC = RecordCodecBuilder.create(i -> i.group(
            RegistryCodecs.homogeneousList(Registries.FLUID).fieldOf("fluids").forGetter(EngineFuelType::fluids),
            Codec.FLOAT.optionalFieldOf("speed", 1f).forGetter(EngineFuelType::speed),
            Codec.FLOAT.optionalFieldOf("efficiency", 1f).forGetter(EngineFuelType::efficiency),
            Codec.FLOAT.optionalFieldOf("torque", 1f).forGetter(EngineFuelType::torque)
    ).apply(i, EngineFuelType::new));

    public static Optional<Holder.Reference<EngineFuelType>> getTypeForFluid(RegistryAccess registryAccess, Fluid fluid) {
        return typeCache.computeIfAbsent(fluid, f -> registryAccess.lookupOrThrow(TFMGRegistries.ENGINE_FUEL_TYPE)
                .listElements()
                .filter(ref -> ref.value().fluids.contains(f.builtInRegistryHolder()))
                .findFirst());
    }

    public boolean test(FluidStack fluidStack) {
        return fluidStack.is(fluids);
    }

    public static boolean test(FluidStack fluidStack, TagKey<EngineFuelType> fuelTypeTag, RegistryAccess registryAccess) {
        HolderGetter<EngineFuelType> lookup = registryAccess.asGetterLookup().lookupOrThrow(TFMGRegistries.ENGINE_FUEL_TYPE);
        var tagged = lookup.getOrThrow(fuelTypeTag);
        return tagged.stream().map(Holder::value).anyMatch(type -> type.test(fluidStack));
    }

    public static class Builder {
        private final HolderSet<Fluid> fluids;
        private float speed = 1f;
        private float efficiency = 1f;
        private float torque = 1f;

        @ApiStatus.Internal
        public Builder() {
            this.fluids = HolderSet.empty();
        }

        public Builder(TagKey<Fluid> fluidTag) {
            this.fluids = BuiltInRegistries.FLUID.getOrCreateTag(fluidTag);
        }

        public Builder(Fluid... fluids) {
            List<Holder<Fluid>> fluidList = new ArrayList<>();
            for (Fluid fluid : fluids) {
                fluidList.add(FluidHelper.convertToStill(fluid).builtInRegistryHolder());
            }
            this.fluids = HolderSet.direct(fluidList);
        }

        public Builder speed(float speed) {
            this.speed = speed;
            return this;
        }

        public Builder efficiency(float efficiency) {
            this.efficiency = efficiency;
            return this;
        }

        public Builder torque(float torque) {
            this.torque = torque;
            return this;
        }

        public EngineFuelType build() {
            return new EngineFuelType(this.fluids, speed, efficiency, torque);
        }
    }
}
