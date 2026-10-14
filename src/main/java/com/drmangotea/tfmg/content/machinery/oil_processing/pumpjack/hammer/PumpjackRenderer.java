package com.drmangotea.tfmg.content.machinery.oil_processing.pumpjack.hammer;

import com.drmangotea.tfmg.content.machinery.oil_processing.pumpjack.base.PumpjackBaseBlockEntity;
import com.drmangotea.tfmg.content.machinery.oil_processing.pumpjack.crank.PumpjackCrankBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import static com.drmangotea.tfmg.base.blocks.TFMGHorizontalDirectionalBlock.FACING;


public class PumpjackRenderer extends SafeBlockEntityRenderer<PumpjackBlockEntity> {
    public PumpjackRenderer(BlockEntityRendererProvider.Context context) {
        super();
    }

    @Override
    protected void renderSafe(
		PumpjackBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay
	) {
		PumpjackCrankBlockEntity crank = be.multiblock.getCrankBE();
		PumpjackBaseBlockEntity base = be.multiblock.getBaseBE();
        if(crank == null || base == null || !be.running)
            return;
		
		Direction direction = be.getBlockState().getValue(FACING);
		int q = be.multiblock.headAtFront ? 1 : -1;
		
		//sides
		float hModifier = crank.heightModifier - be.multiblock.crankConnectorDistance;
		double height2 = Math.pow(crank.heightModifier, 2);
		float x = getX(crank.crankRadius, crank.angle, height2, direction);
		float y = (float) (be.multiblock.crankConnectorDistance*(1 - be.multiblock.crankConnectorDistance) - height2)*q;
		int dist = be.multiblock.connectorDistance*q;
		
        renderPumpjackLink(false, ms, buffer, direction, x, y, hModifier, dist, be.multiblock.crankConnectorDistance);
        renderPumpjackLink(true, ms, buffer, direction, x, y, hModifier, dist, be.multiblock.crankConnectorDistance);
		
		//front
        renderFrontPumpjackLink(ms, buffer, direction, be.multiblock.headBaseDistance, be.multiblock.headDistance, be.angle);
    }
	
	private static float getX(float crankRadius, float angle, double height2, Direction direction) {
		final double sqrt = Math.sqrt(Math.pow(crankRadius, 2) - height2);
		return (float) switch (direction) {
			case WEST, EAST -> {
				if ((angle > 0 && angle < 90 || angle > 270) ||
					(angle < 0 && angle > -90 || angle < -270)) {
					yield sqrt;
				} else {
					yield -sqrt;
				}
			}
			case NORTH, SOUTH -> {
				if ((angle > 90 && angle < 270) || (angle < -90 && angle > -270)) {
					yield sqrt;
				} else {
					yield -sqrt;
				}
			}
			default -> 0f;
		};
	}
	
    private void renderPumpjackLink(
		boolean second, PoseStack pMatrixStack, MultiBufferSource pBuffer,
		Direction direction, float x, float y, float hModifier, int dist, int crankDist
	) {
		pMatrixStack.pushPose();
		
		Vec3 vec3 = new Vec3(0, crankDist,0);
  
		switch (direction) {
			case NORTH -> {
				pMatrixStack.translate(second?0:1, hModifier + 1.5, dist + .5 + x);
				vec3 = vec3.add(0, 0, -x + y);
			}
			case SOUTH -> {
				pMatrixStack.translate(second?1:0, hModifier + 1.5, -dist + .5 + x);
				vec3 = vec3.add(0, 0, -x - y);
			}
			case WEST -> {
				pMatrixStack.translate(dist + .5 + x, hModifier + 1.5, second?1:0);
				vec3 = vec3.add(-x - y, 0, 0);
			}
			case EAST -> {
				pMatrixStack.translate(-dist + .5 + x, hModifier + 1.5, second?0:1);
				vec3 = vec3.add(-x + y, 0, 0);
			}
		}
		
        Matrix4f matrix4f = pMatrixStack.last().pose();
		renderStraightLink(pBuffer, matrix4f, vec3);
        pMatrixStack.popPose();
    }
	
	private static void renderStraightLink(MultiBufferSource pBuffer, Matrix4f matrix, Vec3 vec3) {
		double dist = Mth.invSqrt(vec3.x * vec3.x + vec3.z*vec3.z) * 0.0125F;
		float dz = (float) (vec3.z * dist);
		float dx = (float) (vec3.x * dist);
		
		VertexConsumer consumer = pBuffer.getBuffer(RenderType.leash());
		float px = (float)(vec3.x);
		float py = (float)(vec3.y);
		float pz = (float)(vec3.z);
		int light = LightTexture.pack(15, 15);
		for (int i1 = 0; i1 <= 24; ++i1) {
			float f = (float)i1 / 24.0F;
			float f1 = i1 % 2 == 0 ? 0.07F : 0.1F;
			float X = px * f;
			float Y = py * f;
			float Z = pz * f;
			consumer.addVertex(matrix, X - dx, Y + 0.025F, Z + dz).setColor(f1, f1, f1, 1.0F).setLight(light);
			consumer.addVertex(matrix, X + dx, Y, Z - dz).setColor(f1, f1, f1, 1.0F).setLight(light);
		}
		for (int j1 = 24; j1 >= 0; --j1) {
			float f = (float)j1 / 24.0F;
			float f1 = j1 % 2 == (1) ? 0.07F : 0.1F;
			float X = px * f;
			float Y = py * f;
			float Z = pz * f;
			consumer.addVertex(matrix, X - dx, Y, Z + dz).setColor(f1, f1, f1, 1.0F).setLight(light);
			consumer.addVertex(matrix, X + dx, Y + 0.025F, Z - dz).setColor(f1, f1, f1, 1.0F).setLight(light);
		}
	}
	
	private void renderFrontPumpjackLink (
		PoseStack pMatrixStack, MultiBufferSource pBuffer, Direction direction,
		float linkLength, float headDistance, float angle
	) {
        pMatrixStack.pushPose();
        Vec3 vec3 = new Vec3(0,linkLength,0);
        
		float hModifier = (float) (headDistance * Math.sin(angle * Mth.DEG_TO_RAD));
		
		switch (direction) {
			case NORTH -> {
				pMatrixStack.translate(0.5, -linkLength + 2, .5 - headDistance);
				vec3 = vec3.add(0, hModifier - 0.3,-0.01f);
			}
			case SOUTH -> {
				pMatrixStack.translate(0.5, -linkLength + 2, .5 + headDistance);
				vec3 = vec3.add(0,-hModifier - 0.3, 0.01f);
			}
			case WEST -> {
				pMatrixStack.translate(.5 - headDistance, -linkLength + 2, 0.5);
				vec3 = vec3.add( 0.01f, -hModifier - 0.3, 0);
			}
			case EAST -> {
				pMatrixStack.translate(.5 + headDistance, -linkLength + 2, 0.5);
				vec3 = vec3.add(-0.01f, hModifier - 0.3, 0);
			}
		}
		
		Matrix4f matrix4f = pMatrixStack.last().pose();
		renderLink(pBuffer, matrix4f, vec3);
        pMatrixStack.popPose();
    }
	
	private static void renderLink(MultiBufferSource pBuffer, Matrix4f matrix, Vec3 vec3) {
		double dist = Mth.invSqrt(vec3.x * vec3.x + vec3.z*vec3.z) * 0.0125F;
		float dz = (float) (vec3.z * dist);
		float dx = (float) (vec3.x * dist);
		
		VertexConsumer consumer = pBuffer.getBuffer(RenderType.leash());
		float px = (float)(vec3.x);
		float py = (float)(vec3.y);
		float pz = (float)(vec3.z);
		int light = LightTexture.pack(15, 15);
		for(int i1 = 0; i1 <= 24; ++i1) {
			float f = (float)i1 / 24.0F;
			float f1 = i1 % 2 == 0 ? 0.07F : 0.1F;
			float X = px * f;
			float Y = py * f * (py > 0.0F ? f : 2 - f);
			float Z = pz * f;
			consumer.addVertex(matrix, X - dx, Y + 0.025F, Z + dz).setColor(f1, f1, f1, 1.0F).setLight(light);
			consumer.addVertex(matrix, X + dx, Y + 0.025F - 0.025F, Z - dz).setColor(f1, f1, f1, 1.0F).setLight(light);
		}
		for(int j1 = 24; j1 >= 0; --j1) {
			float f = (float)j1 / 24.0F;
			float f1 = j1 % 2 == (1) ? 0.07F : 0.1F;
			float X = px * f;
			float Y = py * f * (py > 0.0F ? f : 2 - f);
			float Z = pz * f;
			consumer.addVertex(matrix, X - dx, Y, Z + dz).setColor(f1, f1, f1, 1.0F).setLight(light);
			consumer.addVertex(matrix, X + dx, Y + 0.025F, Z - dz).setColor(f1, f1, f1, 1.0F).setLight(light);
		}
	}
}