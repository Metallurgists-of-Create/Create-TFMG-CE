package com.drmangotea.tfmg.registry;

import com.drmangotea.tfmg.TFMG;
import com.drmangotea.tfmg.TFMGRegistries;
import com.drmangotea.tfmg.content.engines.fuels.EngineFuelType;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class TFMGEngineFuelTypes {
    public static final ResourceKey<EngineFuelType> FALLBACK = key("fallback");

    public static final ResourceKey<EngineFuelType> GASOLINE = key("gasoline");
    public static final ResourceKey<EngineFuelType> DIESEL = key("diesel");
    public static final ResourceKey<EngineFuelType> LPG = key("lpg");
    public static final ResourceKey<EngineFuelType> KEROSENE = key("kerosene");
    public static final ResourceKey<EngineFuelType> NAPHTHA = key("naphtha");
    public static final ResourceKey<EngineFuelType> CREOSOTE = key("creosote");
    public static final ResourceKey<EngineFuelType> FURNACE_GAS = key("furnace_gas");

    public static final ResourceKey<EngineFuelType> BIODIESEL = key("chemica", "biodiesel");
    public static final ResourceKey<EngineFuelType> ETHANOL = key("chemica", "ethanol");
    public static final ResourceKey<EngineFuelType> HIGH_CETANE_DIESEL = key("chemica", "high_cetane_diesel");
    public static final ResourceKey<EngineFuelType> HIGH_OCTANE_GASOLINE = key("chemica", "high_octane_gasoline");
    public static final ResourceKey<EngineFuelType> HYDROGEN_FUEL = key("chemica", "hydrogen_fuel");

    private static ResourceKey<EngineFuelType> key(String name) {
        return ResourceKey.create(TFMGRegistries.ENGINE_FUEL_TYPE, TFMG.asResource(name));
    }

    private static ResourceKey<EngineFuelType> key(String namespace, String path) {
        return ResourceKey.create(TFMGRegistries.ENGINE_FUEL_TYPE, ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    public static void bootstrap(BootstrapContext<EngineFuelType> ctx) {
        register(ctx, FALLBACK, new EngineFuelType.Builder()
                .speed(1)
                .efficiency(1)
                .torque(1)
                .build());

        register(ctx, GASOLINE, new EngineFuelType.Builder(TFMGTags.Fluids.GASOLINE.tag)
                .speed(1)
                .efficiency(1)
                .torque(1)
                .build());

        register(ctx, DIESEL, new EngineFuelType.Builder(TFMGTags.Fluids.DIESEL.tag)
                .speed(0.8f)
                .efficiency(0.8f)
                .torque(1.4f)
                .build());

        register(ctx, LPG, new EngineFuelType.Builder(TFMGTags.Fluids.LPG.tag)
                .speed(1.2f)
                .efficiency(0.7f)
                .torque(0.7f)
                .build());

        register(ctx, KEROSENE, new EngineFuelType.Builder(TFMGTags.Fluids.KEROSENE.tag)
                .speed(0.7f)
                .efficiency(1f)
                .torque(1.4f)
                .build());

        register(ctx, NAPHTHA, new EngineFuelType.Builder(TFMGTags.Fluids.NAPHTHA.tag)
                .speed(1f)
                .efficiency(0.7f)
                .torque(1.3f)
                .build());

        register(ctx, CREOSOTE, new EngineFuelType.Builder(TFMGTags.Fluids.CREOSOTE.tag)
                .speed(0.7f)
                .efficiency(0.4f)
                .torque(0.5f)
                .build());

        register(ctx, FURNACE_GAS, new EngineFuelType.Builder(TFMGTags.Fluids.FURNACE_GAS.tag)
                .speed(0.5f)
                .efficiency(0.3f)
                .torque(0.3f)
                .build());

        //Chemica fix

        register(ctx, BIODIESEL, new EngineFuelType.Builder(TFMGTags.Fluids.BIODIESEL.tag)
                .speed(1.0f)
                .efficiency(0.9f)
                .torque(0.8f)
                .build());

        register(ctx, ETHANOL, new EngineFuelType.Builder(TFMGTags.Fluids.ETHANOL.tag)
                .speed(0.6f)
                .efficiency(0.8f)
                .torque(0.6f)
                .build());

        register(ctx, HIGH_CETANE_DIESEL, new EngineFuelType.Builder(TFMGTags.Fluids.HIGH_CETANE_DIESEL.tag)
                .speed(1.8f)
                .efficiency(1.0f)
                .torque(2.1f)
                .build());

        register(ctx, HIGH_OCTANE_GASOLINE, new EngineFuelType.Builder(TFMGTags.Fluids.HIGH_OCTANE_GASOLINE.tag)
                .speed(1.6f)
                .efficiency(1.2f)
                .torque(2.3f)
                .build());

        register(ctx, HYDROGEN_FUEL, new EngineFuelType.Builder(TFMGTags.Fluids.HYDROGEN_FUEL.tag)
                .speed(1.8f)
                .efficiency(1.3f)
                .torque(0.8f)
                .build());
    }

    private static void register(BootstrapContext<EngineFuelType> ctx, ResourceKey<EngineFuelType> name, EngineFuelType type) {
        ctx.register(name, type);
    }
}
