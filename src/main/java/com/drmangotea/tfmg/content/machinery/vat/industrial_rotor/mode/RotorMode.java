package com.drmangotea.tfmg.content.machinery.vat.industrial_rotor.mode;

import com.drmangotea.tfmg.TFMGRegistries;
import com.drmangotea.tfmg.content.machinery.vat.MultiUseAttachment;
import com.drmangotea.tfmg.content.machinery.vat.base.VatBlockEntity;
import com.drmangotea.tfmg.content.machinery.vat.base.registry.operations.VatOperationEntry;
import com.drmangotea.tfmg.content.machinery.vat.industrial_rotor.IndustrialRotorBlockEntity;
import com.drmangotea.tfmg.registry.TFMGRotorModes;
import com.drmangotea.tfmg.registry.TFMGVatOperations;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.Util;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class RotorMode implements MultiUseAttachment<IndustrialRotorBlockEntity> {
    private String descriptionId;
    private final ResourceLocation id;
    private final VatOperationEntry operation;
    private final RotorPartial rotorPartial;

    public RotorMode(Properties properties) {
        this.id = properties.id;
        this.operation = properties.operation;
        this.rotorPartial = properties.rotorPartial;
    }

    public boolean isNone() {
        return this != TFMGRotorModes.NONE.get();
    }

    @Override
    public VatOperationEntry getOperation() {
        return this.operation;
    }

    @Override
    public void renderInVat(IndustrialRotorBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay, @Nullable ItemRenderer itemRenderer) {
        if (this.rotorPartial != null && be.getLevel() != null) {
            BlockState blockState = be.getBlockState();
            int height = be.vatHeight;
            for (int i = 0; i < height; i++) {
                PartialModel model = rotorPartial.getPartial(i, height, be);
                float posX = be.vatSize == 2 ? (be.vatPos.getX() - be.getBlockPos().getX() + 0.5f) : 0f;
                float posZ = be.vatSize == 2 ? (be.vatPos.getZ() - be.getBlockPos().getZ() + 0.5f) : 0f;
                CachedBuffers.partial(model, blockState)
                        .light(LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos().below()))
                        .center()
                        .translate(posX, -i - 1 + be.pulledAmount, posZ)
                        .uncenter()
                        .renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));
            }
        }
    }

    @Override
    public void tick(VatBlockEntity controllerVat, Level level, BlockPos pos, boolean active, boolean clientTick) {}

    public String getOrCreateDescriptionId() {
        if (this.descriptionId == null) {
            this.descriptionId = Util.makeDescriptionId("rotor_mode", getKey());
        }

        return this.descriptionId;
    }

    public String getDescriptionId() {
        return getOrCreateDescriptionId();
    }

    public Component getDisplayName() {
        return Component.translatable(this.getOrCreateDescriptionId());
    }

    public ResourceLocation getKey() {
        return this.id;
    }

    public static class Properties {
        private final ResourceLocation id;
        VatOperationEntry operation = TFMGVatOperations.NONE;
        RotorPartial rotorPartial = null;

        public Properties(ResourceLocation id) {
            this.id = id;
        }

        public Properties operation(VatOperationEntry operation) {
            this.operation = operation;
            return this;
        }

        public Properties partial(RotorPartial partial) {
            this.rotorPartial = partial;
            return this;
        }
    }

    public record Stored(Holder<RotorMode> mode) {
        public static final Codec<Stored> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                TFMGRegistries.ROTOR_MODE_REGISTRY.holderByNameCodec().fieldOf("mode").forGetter(Stored::mode)
        ).apply(inst, Stored::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, Stored> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.holderRegistry(TFMGRegistries.ROTOR_MODE), Stored::mode,
                Stored::new
        );

        public static final RotorMode.Stored NONE = new Stored(TFMGRotorModes.NONE);
    }
}
