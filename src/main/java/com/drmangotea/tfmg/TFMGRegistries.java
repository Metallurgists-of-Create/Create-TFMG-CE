package com.drmangotea.tfmg;

import com.drmangotea.tfmg.content.electricity.connection.cable_type.CableType;
import com.drmangotea.tfmg.content.engines.fuels.EngineFuelType;
import com.drmangotea.tfmg.content.engines.types.EngineType;
import com.drmangotea.tfmg.content.items.weapons.fire_extinguisher.FireExtinguisherFuelType;
import com.drmangotea.tfmg.content.items.weapons.flamethrover.FlamethrowerFuelType;
import com.drmangotea.tfmg.content.machinery.vat.electrode_holder.electrode.Electrode;
import com.drmangotea.tfmg.content.machinery.vat.industrial_mixer.mode.MixerMode;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class TFMGRegistries {
    public static final ResourceKey<Registry<CableType>> CABLE_TYPE = createRegistryKey("cable_types");
    public static final ResourceKey<Registry<Electrode>> ELECTRODE = createRegistryKey("electrodes");
    public static final ResourceKey<Registry<EngineType>> ENGINE_TYPE = createRegistryKey("engine_type");
    public static final ResourceKey<Registry<MixerMode>> MIXER_MODE = createRegistryKey("mixer_mode");

    public static final ResourceKey<Registry<FlamethrowerFuelType>> FLAMETHROWER_FUEL_TYPE = createRegistryKey("fuel_type/flamethrower");
    public static final ResourceKey<Registry<EngineFuelType>> ENGINE_FUEL_TYPE = createRegistryKey("fuel_type/engine");
    public static final ResourceKey<Registry<FireExtinguisherFuelType>> FIRE_EXTINGUISHER_FUEL_TYPE = createRegistryKey("fuel_type/fire_extinguisher");

    public static final Registry<CableType> CABLE_TYPE_REGISTRY = makeSyncedRegistry(CABLE_TYPE);
    public static final Registry<Electrode> ELECTRODE_REGISTRY = makeSyncedRegistry(ELECTRODE);
    public static final Registry<EngineType> ENGINE_TYPE_REGISTRY = makeSyncedRegistry(ENGINE_TYPE);
    public static final Registry<MixerMode> MIXER_MODE_REGISTRY = makeSyncedRegistry(MIXER_MODE);


    private static <T> ResourceKey<Registry<T>> createRegistryKey(String name) {
        return ResourceKey.createRegistryKey(TFMG.asResource(name));
    }

    private static <T> Registry<T> makeSyncedRegistry(ResourceKey<Registry<T>> registryKey) {
        return new RegistryBuilder<>(registryKey).sync(true).create();
    }

    private static <T> Registry<T> makeRegistry(ResourceKey<Registry<T>> registryKey) {
        return new RegistryBuilder<>(registryKey).create();
    }
}
