package com.drmangotea.tfmg.content.machinery.vat.industrial_rotor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class IndustrialRotorRenderer extends KineticBlockEntityRenderer<IndustrialRotorBlockEntity> {
    public IndustrialRotorRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(IndustrialRotorBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
        if (!be.rotorMode.isValid()) return;
        if (!Minecraft.getInstance().isPaused()) {
            be.pulledAmount += be.pullSpeed.getValue(partialTicks) * 3 / 10f;
            be.pulledAmount = Math.clamp(be.pulledAmount, 0, be.getMaxPullDistance());
        }

        be.rotorMode.renderInVat(be, partialTicks, ms, buffer, light, overlay, null);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(IndustrialRotorBlockEntity be, BlockState state) {
        return CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state, state.getValue(IndustrialRotorBlock.HORIZONTAL_FACING));
    }
}
