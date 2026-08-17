package com.drmangotea.tfmg.content.engines.fuels;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

@Deprecated(since = "1.2.4")
public class FuelType {

    @Deprecated(since = "1.2.4")
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
