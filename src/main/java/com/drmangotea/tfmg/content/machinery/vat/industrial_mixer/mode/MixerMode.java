package com.drmangotea.tfmg.content.machinery.vat.industrial_mixer.mode;

import com.drmangotea.tfmg.TFMGRegistries;
import com.drmangotea.tfmg.content.machinery.vat.base.VatBlockEntity;
import com.drmangotea.tfmg.content.machinery.vat.industrial_mixer.IndustrialMixerBlockEntity;
import com.drmangotea.tfmg.registry.TFMGMixerModes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.Util;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class MixerMode {
    private String descriptionId;
    private final ResourceLocation id;
    private final ResourceLocation operation;
    private final MixerPartial mixerPartial;

    public MixerMode(Properties properties) {
        this.id = properties.id;
        this.operation = properties.operation;
        this.mixerPartial = properties.mixerPartial;
    }

    public ResourceLocation getOperationId() {
        return this.operation;
    }

    public void renderInVat(IndustrialMixerBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        if (this.mixerPartial != null) {
            BlockState blockState = be.getBlockState();
            int height = be.vatHeight;
            for (int i = 0; i < height; i++) {
                PartialModel model = mixerPartial.getPartial(i, height, be);
                float posX = be.vatSize == 2 ? (be.vatPos.getX() - be.getBlockPos().getX() + 0.5f) : 0f;
                float posZ = be.vatSize == 2 ? (be.vatPos.getZ() - be.getBlockPos().getZ() + 0.5f) : 0f;
                CachedBuffers.partial(model, blockState)
                        .light(LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos().below()))
                        .center()
                        .translate(posX, -i - 1, posZ)
                        .rotateYDegrees(be.angle)
                        .uncenter()
                        .renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));
            }
        }
    }

    public void tick(VatBlockEntity controllerVat, Level level, BlockPos pos, boolean active, boolean clientTick) {

    }

    public boolean isValid() {
        return getOperationId() != null;
    }

    public String getOrCreateDescriptionId() {
        if (this.descriptionId == null) {
            this.descriptionId = Util.makeDescriptionId("mixer_mode", getKey());
        }

        return this.descriptionId;
    }

    public String getDescriptionId() {
        return this.getOrCreateDescriptionId();
    }

    public Component getDisplayName() {
        return Component.translatable(this.getOrCreateDescriptionId());
    }

    public ResourceLocation getKey() {
        return this.id;
    }

    public static class Properties {
        private ResourceLocation id;

        ResourceLocation operation = null;
        MixerPartial mixerPartial = null;

        public Properties operation(ResourceLocation operation) {
            this.operation = operation;
            return this;
        }

        public Properties partial(MixerPartial partial) {
            this.mixerPartial = partial;
            return this;
        }

        public Properties(ResourceLocation id) {
            this.id = id;
        }
    }

    public record Stored(Holder<MixerMode> mode) {
        public static final Codec<Stored> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                TFMGRegistries.MIXER_MODE_REGISTRY.holderByNameCodec().fieldOf("mode").forGetter(Stored::mode)
        ).apply(instance, Stored::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, Stored> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.holderRegistry(TFMGRegistries.MIXER_MODE),
                Stored::mode,
                Stored::new
        );

        public static final MixerMode.Stored NONE = new Stored(TFMGMixerModes.none);
    }

    public static class Mixer extends MixerMode {
        public Mixer(Properties properties) {
            super(properties);
        }

        public void renderInVat(IndustrialMixerBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        }
    }
}
