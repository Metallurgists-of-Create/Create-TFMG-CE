package com.drmangotea.tfmg.content.machinery.vat.industrial_mixer;

import com.drmangotea.tfmg.registry.TFMGPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class IndustrialMixerRenderer extends KineticBlockEntityRenderer<IndustrialMixerBlockEntity> {


    public IndustrialMixerRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(IndustrialMixerBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
        if (!be.mixerMode.isValid())
            return;
        if (!Minecraft.getInstance().isPaused()) {
            be.angle += be.visualSpeed.getValue(partialTicks) * 3 / 10f;
            be.angle %= 360;
        }

        be.mixerMode.renderInVat(be, partialTicks, ms, buffer, light, overlay, null);
    }

    public static PartialModel getCentrifugeModel(int currentHeight, int totalHeight, IndustrialMixerBlockEntity be) {
        if (be.vatSize == 1) {
            if (totalHeight == 1)
                return TFMGPartialModels.SMALL_CENTRIFUGE_ALONE;
            if (currentHeight == 0)
                return TFMGPartialModels.SMALL_CENTRIFUGE_TOP;
            if (currentHeight == totalHeight)
                return TFMGPartialModels.SMALL_CENTRIFUGE_BOTTOM;
            return TFMGPartialModels.SMALL_CENTRIFUGE_MIDDLE;
        } else {
            if (totalHeight == 1)
                return TFMGPartialModels.LARGE_CENTRIFUGE_ALONE;

            if (currentHeight == 0)
                return TFMGPartialModels.LARGE_CENTRIFUGE_TOP;
            if (currentHeight == totalHeight)
                return TFMGPartialModels.LARGE_CENTRIFUGE_BOTTOM;
            return TFMGPartialModels.LARGE_CENTRIFUGE_MIDDLE;
        }
    }

    public static PartialModel getMixerModel(int i, int height, IndustrialMixerBlockEntity be) {
        if (i == height - 1) {
            if (be.vatSize > 1) {
                return TFMGPartialModels.MIXER;
            } else {
                return TFMGPartialModels.SMALL_MIXER;
            }
        } else {
            return TFMGPartialModels.MIXER_SHAFT;
        }
    }

    @Override
    protected SuperByteBuffer getRotatedModel(IndustrialMixerBlockEntity be, BlockState state) {
        return CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state, Direction.UP);
    }
}

