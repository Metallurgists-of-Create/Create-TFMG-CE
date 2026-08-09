package com.drmangotea.tfmg.registry;

import com.drmangotea.tfmg.TFMG;
import com.drmangotea.tfmg.TFMGRegistries;
import com.drmangotea.tfmg.content.engines.fuel.EngineFuelType;
import com.drmangotea.tfmg.content.items.weapons.flamethrover.FlamethrowerFuelType;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;

public class TFMGEngineFuelTypes {
    public static final ResourceKey<EngineFuelType> FALLBACK = ResourceKey.create(TFMGRegistries.ENGINE_FUEL_TYPE, TFMG.asResource("fallback"));

    public static void bootstrap(BootstrapContext<EngineFuelType> ctx) {
        register(ctx, "fallback", new EngineFuelType.Builder()
                .speed(1)
                .efficiency(1)
                .torque(1)
                .build());

        register(ctx, "gasoline", new EngineFuelType.Builder(TFMGTags.TFMGFluidTags.GASOLINE.tag)
                .speed(1)
                .efficiency(1)
                .torque(1)
                .build());

        register(ctx, "diesel", new EngineFuelType.Builder(TFMGTags.TFMGFluidTags.DIESEL.tag)
                .speed(0.8f)
                .efficiency(0.8f)
                .torque(1.4f)
                .build());

        register(ctx, "lpg", new EngineFuelType.Builder(TFMGTags.TFMGFluidTags.LPG.tag)
                .speed(1.2f)
                .efficiency(0.7f)
                .torque(0.7f)
                .build());

        register(ctx, "kerosene", new EngineFuelType.Builder(TFMGTags.TFMGFluidTags.KEROSENE.tag)
                .speed(0.7f)
                .efficiency(1f)
                .torque(1.4f)
                .build());

        register(ctx, "naphtha", new EngineFuelType.Builder(TFMGTags.TFMGFluidTags.NAPHTHA.tag)
                .speed(1f)
                .efficiency(0.7f)
                .torque(1.3f)
                .build());

        register(ctx, "creosote", new EngineFuelType.Builder(TFMGTags.TFMGFluidTags.CREOSOTE.tag)
                .speed(0.7f)
                .efficiency(0.4f)
                .torque(0.5f)
                .build());

        register(ctx, "furnace_gas", new EngineFuelType.Builder(TFMGTags.TFMGFluidTags.FURNACE_GAS.tag)
                .speed(0.5f)
                .efficiency(0.3f)
                .torque(0.3f)
                .build());
    }

    private static void register(BootstrapContext<EngineFuelType> ctx, String name, EngineFuelType type) {
        ctx.register(ResourceKey.create(TFMGRegistries.ENGINE_FUEL_TYPE, TFMG.asResource(name)), type);
    }
}
