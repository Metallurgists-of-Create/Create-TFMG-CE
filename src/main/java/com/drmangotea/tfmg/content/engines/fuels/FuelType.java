package com.drmangotea.tfmg.content.engines.fuels;

import com.drmangotea.tfmg.registry.TFMGEngineFuelTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

/**
 * This exists purely so Chemica can load as it tries to register engine fuels to the legacy registry.
 * @deprecated See {@link TFMGEngineFuelTypes}.
 */
@Deprecated(since = "1.2.4", forRemoval = true)
public class FuelType {

    /**
     * This exists purely so Chemica can load as it tries to register engine fuels to the legacy registry.
     * @deprecated See {@link TFMGEngineFuelTypes}.
     */
    @Deprecated(since = "1.2.4", forRemoval = true)
    public static class Builder {
        public Builder(ResourceLocation id) {}
        public Builder speed(float speed) {return this;}
        public Builder efficiency(float efficiency) {return this;}
        public Builder stress(float stress) {return this;}

        public final Builder addFluids(TagKey<Fluid> tag) {return this;}
        public FuelType register() {return new FuelType();}
        public final FuelType registerAndAssign(TagKey<Fluid> tag) {return register();}
    }
}
