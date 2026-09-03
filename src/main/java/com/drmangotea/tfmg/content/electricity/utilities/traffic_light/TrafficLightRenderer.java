package com.drmangotea.tfmg.content.electricity.utilities.traffic_light;


import com.drmangotea.tfmg.registry.TFMGPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.render.RenderTypes;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

public class TrafficLightRenderer extends SafeBlockEntityRenderer<TrafficLightBlockEntity> {
    public TrafficLightRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    protected void renderSafe(TrafficLightBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        BlockState blockState = be.getBlockState();

        ms.pushPose();

        float glow = be.glow.getValue(partialTicks);


      //  if(be.glow.getValue()!=0) {

        if(be.getData().getVoltage()>0&&be.getPowerUsage()>0)
                CachedBuffers.partialFacing(TFMGPartialModels.TRAFFIC_LIGHT, blockState, blockState.getValue(HorizontalDirectionalBlock.FACING).getOpposite())
                        .light((int) glow * 3 + 80)
                        .color(be.light.color)
                        .disableDiffuse()
                        .translateY(be.light.offset)
                        .renderInto(ms, buffer.getBuffer(RenderTypes.additive()));


      //  }
        ms.popPose();
    }

}
