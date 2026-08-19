package com.drmangotea.tfmg.content.machinery.oil_processing.surface_scanner;

import com.drmangotea.tfmg.integration.sable.SurfaceScannerSable;
import com.drmangotea.tfmg.registry.TFMGPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
		FACING_NORTH = new Quaternionf(0, 0, 0,  1).normalize(), //+z
		FACING_WEST  = new Quaternionf(0, 1, 0,  1).normalize(), //+x
		FACING_SOUTH = new Quaternionf(0, 1, 0,  0).normalize(), //-z
		FACING_EAST  = new Quaternionf(0, 1, 0, -1).normalize(); //-x
	
    public SurfaceScannerRenderer(BlockEntityRendererProvider.Context context) {}
	
	private Quaternionf getFacingQuat (SurfaceScannerBlockEntity be) {
		//I feel like there should be a way to optimise this further, but it works -Shallow
		Quaterniond q1 = SurfaceScannerSable.getSublevelRot(be).conjugate();
		Quaternionf q = new Quaternionf(0, q1.y, 0, q1.w).normalize();
		float
			north = q.dot(FACING_NORTH),
			west = q.dot(FACING_WEST),
			south = q.dot(FACING_SOUTH),
			east = q.dot(FACING_EAST);
		float dot = Math.min(Math.min(north,west),Math.min(south,east));
		if (dot == north) return FACING_NORTH;
		if (dot == west) return FACING_WEST;
		if (dot == south) return FACING_SOUTH;
		if (dot == east) return FACING_EAST;
		return FACING_NORTH;
	}
	
    @Override
    protected void renderSafe(SurfaceScannerBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource bufferSource, int light, int overlay) {
        BlockState blockState = be.getBlockState();
		VertexConsumer buffer = bufferSource.getBuffer(RenderTypes.additive());
        ms.pushPose();
		ms.rotateAround(getFacingQuat(be), 0.5f, 0.5f, 0.5f);
  
		for(int x = 0;x<5;x++) { for (int z = 0; z < 5; z++) { if(be.grid[x][z]) {
			CachedBuffers.partial(TFMGPartialModels.SURFACE_SCANNER_LIGHT, blockState)
				.translate((x - 2)*0.19, 0, (z - 2)*0.19)
				.light(LightTexture.FULL_BRIGHT)
				.color(255, 69, 96, 255) //#ff4560ff
				.renderInto(ms, buffer);
		} } }
        ms.popPose();
    }
}