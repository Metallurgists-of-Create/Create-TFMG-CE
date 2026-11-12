package com.drmangotea.tfmg.content.engines.fuels;

import com.drmangotea.tfmg.TFMGRegistries;
import com.drmangotea.tfmg.registry.TFMGFluids;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.foundation.fluid.FluidHelper;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public record EngineFuelType(HolderSet<Fluid> fluids, float speed, float efficiency, float torque, Fluid exhaust) {
    public static final Map<Fluid, Optional<Holder.Reference<EngineFuelType>>> typeCache = new HashMap<>();

    public static final Codec<EngineFuelType> CODEC = RecordCodecBuilder.create(i -> i.group(
		RegistryCodecs.homogeneousList(Registries.FLUID).fieldOf("fluids").forGetter(EngineFuelType::fluids),
		Codec.FLOAT.optionalFieldOf("speed", 1f).forGetter(EngineFuelType::speed),
		Codec.FLOAT.optionalFieldOf("efficiency", 1f).forGetter(EngineFuelType::efficiency),
		Codec.FLOAT.optionalFieldOf("torque", 1f).forGetter(EngineFuelType::torque),
		BuiltInRegistries.FLUID.byNameCodec().optionalFieldOf("exhaust", TFMGFluids.CARBON_DIOXIDE.get()).forGetter(EngineFuelType::exhaust)
    ).apply(i, EngineFuelType::new));

    public static Optional<Holder.Reference<EngineFuelType>> getTypeForFluid(RegistryAccess registryAccess, Fluid fluid) {
        return typeCache.computeIfAbsent(fluid, f -> {
			Optional<Holder.Reference<Fluid>> fluidRef = BuiltInRegistries.FLUID.getHolder(BuiltInRegistries.FLUID.getKey(f));
            return fluidRef.flatMap(fluidReference -> registryAccess.lookupOrThrow(TFMGRegistries.ENGINE_FUEL_TYPE)
                    .listElements()
                    .filter(ref -> ref.value().fluids.contains(fluidReference))
                    .findFirst());

        });
    }

    public boolean test(FluidStack fluidStack) {
        return fluidStack.is(fluids);
    }

    public static boolean test(FluidStack fluidStack, TagKey<EngineFuelType> fuelTypeTag, RegistryAccess registryAccess) {
        HolderGetter<EngineFuelType> lookup = registryAccess.asGetterLookup().lookupOrThrow(TFMGRegistries.ENGINE_FUEL_TYPE);
        var tagged = lookup.get(fuelTypeTag);
        return tagged.map(holders -> holders.stream().map(Holder::value).anyMatch(type -> type.test(fluidStack))).orElse(false);
    }

    public static Optional<EngineFuelType> find(FluidStack fluidStack, RegistryAccess registryAccess, @Nullable TagKey<EngineFuelType> tagFilter) {
		Stream<? extends Holder<EngineFuelType>> stream;
        if (tagFilter == null) {
			stream = registryAccess.registryOrThrow(TFMGRegistries.ENGINE_FUEL_TYPE).holders();
		} else {
            stream = registryAccess.asGetterLookup()
				.lookupOrThrow(TFMGRegistries.ENGINE_FUEL_TYPE)
				.get(tagFilter).orElseThrow().stream();
		}
		return stream.map(Holder::value).filter(type -> type.test(fluidStack)).findFirst();
	}

    public static class Builder {
        private final HolderSet<Fluid> fluids;
        private float speed = 1f;
        private float efficiency = 1f;
        private float torque = 1f;
		private Fluid exhaust = TFMGFluids.CARBON_DIOXIDE.getSource();

        @ApiStatus.Internal
        public Builder() {
            this.fluids = HolderSet.empty();
        }

        public Builder(TagKey<Fluid> fluidTag) {
            this.fluids = BuiltInRegistries.FLUID.getOrCreateTag(fluidTag);
        }

        public Builder(Fluid... fluids) {
			this.fluids = HolderSet.direct(Arrays.stream(fluids).<Holder<Fluid>>map(fluid -> {
				ResourceLocation f = BuiltInRegistries.FLUID.getKey(FluidHelper.convertToStill(fluid));
				return BuiltInRegistries.FLUID.getHolder(f).orElseThrow();
			}).toList());
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
		
		public Builder exhaust(Fluid exhaust) {
			this.exhaust = FluidHelper.convertToStill(exhaust);
			return this;
		}

        public EngineFuelType build() {
            return new EngineFuelType(this.fluids, speed, efficiency, torque, exhaust);
        }
    }

    @Override
    public int hashCode() {
        return fluids().hashCode() + exhaust().hashCode() + Float.hashCode(speed()) + Float.hashCode(efficiency()) + Float.hashCode(torque());
    }

    @Override
    public @NotNull String toString() {
        return "EngineFuelType[fluids=["
                + fluids.stream()
                .map(h -> h.unwrapKey().map(k -> k.location().toString()).orElse("unregistered"))
                .reduce((a, b) -> a + ", " + b).orElse("")
                + "], exhaust="
                + BuiltInRegistries.FLUID.getKey(this.exhaust)
                + ", speed=" + this.speed
                + ", efficiency=" + this.efficiency
                + ", torque=" + this.torque + "]";
    }
}
