package com.drmangotea.tfmg.content.machinery.oil_processing.surface_scanner;

import com.drmangotea.tfmg.integration.sable.SurfaceScannerSable;
import com.drmangotea.tfmg.registry.TFMGPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.render.RenderTypes;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaterniond;
import org.joml.Quaternionf;

public class SurfaceScannerRenderer extends SafeBlockEntityRenderer<SurfaceScannerBlockEntity> {
	private static final Quaternionf
		FACING_NORTH = new Quaternionf(0, 1, 0, 0), //-z
		FACING_SOUTH = new Quaternionf(0, 0, 0, 1), //+z
		FACING_WEST  = new Quaternionf(0, 1, 0,-1).normalize(), //-x
		FACING_EAST  = new Quaternionf(0, 1, 0, 1).normalize(); //+x
	
    public SurfaceScannerRenderer(BlockEntityRendererProvider.Context context) {}

	private Quaternionf getFacingQuat(SurfaceScannerBlockEntity be) {
		Quaterniond rot = SurfaceScannerSable.getSublevelRot(be);
		double y = -rot.y;
		double w = rot.w;
		Quaternionf best = FACING_NORTH;
		double bestDot = y * FACING_NORTH.y + w * FACING_NORTH.w;
		double d = y * FACING_WEST.y + w * FACING_WEST.w;
		if (d > bestDot) { bestDot = d; best = FACING_WEST; }
		d = y * FACING_SOUTH.y + w * FACING_SOUTH.w;
		if (d > bestDot) { bestDot = d; best = FACING_SOUTH; }
		d = y * FACING_EAST.y + w * FACING_EAST.w;
		if (d > bestDot) { best = FACING_EAST; }
		return best;
	}
	
    @Override
    protected void renderSafe(SurfaceScannerBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource bufferSource, int light, int overlay) {
        BlockState blockState = be.getBlockState();
        ms.pushPose();
		ms.rotateAround(getFacingQuat(be), 0.5f, 0.5f, 0.5f);
  
		for (int x = 0 ; x < 5; x++) {
			for (int z = 0; z < 5; z++) {
				if (be.grid[x][z]) {
					CachedBuffers.partial(TFMGPartialModels.SURFACE_SCANNER_LIGHT, blockState)
						.translate((x - 2)*0.19, 0, (z - 2)*0.19)
						.light(LightTexture.FULL_BRIGHT)
						.color(255, 69, 96, 255) //#ff4560ff
						.renderInto(ms, bufferSource.getBuffer(RenderTypes.additive()));
				}
			}
		}
        ms.popPose();
    }
}