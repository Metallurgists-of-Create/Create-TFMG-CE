package com.drmangotea.tfmg.content.electricity.connection.cables;

import com.drmangotea.tfmg.registry.TFMGDataComponents;
import com.drmangotea.tfmg.registry.TFMGTags;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.awt.*;

public class CableConnectorRenderer extends SafeBlockEntityRenderer<CableConnectorBlockEntity> {
    public CableConnectorRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    protected void renderSafe(CableConnectorBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource bufferSource, int light, int overlay) {
        renderPlayerHeldCable(be, ms, bufferSource, partialTicks);

        for (CableConnection connection : be.connections) {
            if (connection.visible())
                renderWire(be.getLevel(), ms, bufferSource, Vec3.atLowerCornerOf(connection.pos2()), Vec3.atLowerCornerOf(connection.pos1()), connection.getLength() / 4500, connection.type().getColor());
        }
    }

    public void renderPlayerHeldCable(CableConnectorBlockEntity be, PoseStack ms, MultiBufferSource bufferSource, float partialTicks) {
        if (be.player == null)
            return;
        Player player = be.player;
        if (player.getInventory().contains(TFMGTags.TFMGItemTags.SPOOLS.tag)) {
            ItemStack stack = player.getMainHandItem();
            if (stack.has(TFMGDataComponents.POSITION)) {
                BlockPos pos = stack.get(TFMGDataComponents.POSITION);
				if (pos.equals(be.getBlockPos()))
                     renderWire(be.getLevel(), ms, bufferSource, player.position(), Vec3.atLowerCornerOf(pos), 0.0001f, be.color);
            }
        }
    }
	
	private static void renderWire(Level level, PoseStack pMatrixStack, MultiBufferSource pBuffer, Vec3 pos1, Vec3 pos2, float curve, int color) {
		pMatrixStack.pushPose();
		//the offset was hardcoded into the original function - not 100% sure on the why
		Vec3 pos2Local = pos2.subtract(pos1).add(0.01,0,0.01);
		pMatrixStack.translate(0.5, 0.5, 0.5);
		float
			X = (float) pos2Local.x(),
			Y = (float) pos2Local.y(),
			Z = (float) pos2Local.z();
		VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.leash());
		Matrix4f matrix4f = pMatrixStack.last().pose();
		float f4 = Mth.invSqrt(X * X + Z * Z) * 0.0125F;
		float dz = Z * f4;
		float dx = X * f4;
		
		int light1 = level.getBrightness(LightLayer.SKY, new BlockPos((int) pos2.x(), (int) pos2.y(), (int) pos2.z()));
		int light2 = level.getBrightness(LightLayer.SKY, new BlockPos((int) pos1.x(), (int) pos1.y(), (int) pos1.z()));
		
		Color c = new Color(color);
		float
			r = c.getRed() / 255f,
			b = c.getBlue() / 255f,
			g = c.getGreen() / 255f;
		
		for (int i1 = 0; i1 <= 24; ++i1) {
			addVertexPair(vertexconsumer, matrix4f, X, Y, Z, light1, light2, 0.030F, 0.030F, dz, dx, i1, false, curve, r, g, b);
		}
		for (int j1 = 24; j1 >= 0; --j1) {
			addVertexPair(vertexconsumer, matrix4f, X, Y, Z, light1, light2, 0.030F, 0.00F, dz, dx, j1, true, curve, r, g, b);
		}
		pMatrixStack.popPose();
	}
	
	private static void addVertexPair(VertexConsumer vertexConsumer, Matrix4f matrix4f, float px, float py, float pz, int light_1, int light_2, float thickness, float dy, float dx, float dz, int value, boolean reverse, float curve, float r, float g, float b) {
		float f = value / 24.0F;
		int i = (int) Mth.lerp(f, (float) light_1, (float) light_2);
		int light = LightTexture.pack(i, i);
		float brightness = value % 2 == (reverse ? 1 : 0) ? 0.7F : 1.0F;
		r *= brightness;
		g *= brightness;
		b *= brightness;
		
		float x = px * f;
		float y = py * f * (py > 0.0F ? f : (2F - f));
		float z = pz * f;
		
		float pain = value * curve * (value - 24);
		
		vertexConsumer.addVertex(matrix4f, x - dx, y + dy + pain, z + dz).setColor(r, g, b, 1.0F).setLight(light);
		vertexConsumer.addVertex(matrix4f, x + dx, y + thickness - dy + pain, z - dz).setColor(r, g, b, 1.0F).setLight(light);
	}
}