package com.drmangotea.tfmg.content.electricity.experimental;

import com.drmangotea.tfmg.base.TFMGUtils;
import com.drmangotea.tfmg.content.electricity.experimental.simulation.ConnectingElectricalNode;
import com.drmangotea.tfmg.content.electricity.experimental.simulation.ElectricalNode;
import com.drmangotea.tfmg.registry.TFMGPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

public class ElectricNetworkRenderer {


    @OnlyIn(Dist.CLIENT)
    public static void tickRender(RenderLevelStageEvent event) {
        tickOutlines();
        renderWires(event);
    }

    @OnlyIn(Dist.CLIENT)
    public static void renderWires(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES)
            return;
        if (player == null)
            return;
        RealElectricalNetwork network = RealElectricNetworkManager.getNetwork(mc.level);
        LevelRenderer levelRenderer = event.getLevelRenderer();
        PoseStack ms = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        VertexConsumer vc = buffer.getBuffer(RenderType.cutoutMipped());
        Camera camera = event.getCamera();
        Vector3f playerPos = player.getEyePosition().toVector3f();
        // TFMG.LOGGER.debug(network.connections.size()+"");
        renderWire(TFMGPartialModels.CABLE, new Vec3(0, -56, 0), new Vec3(0, -56, 10), camera, ms, vc, player, true);

        for (WireConnection connection : network.connections) {
            renderWire(
				TFMGPartialModels.CABLE,
				connection.node1().getPosition().add(Vec3.atLowerCornerOf(connection.node1().pos)),
				connection.node2().getPosition().add(Vec3.atLowerCornerOf(connection.node2().pos)),
				camera, ms, vc, player, false
			);
        }
    }

    public static void renderWire(PartialModel model, Vec3 pos1, Vec3 pos2, Camera camera, PoseStack ms, VertexConsumer vc, Player player, boolean debug) {
		Vec3 cameraInverse = Vec3.ZERO.subtract(camera.getPosition());

        int cableRenderDistance = 64;

        BlockState air = Blocks.AIR.defaultBlockState();


		pos1.subtract(0.5d,0.5d,0.5d);
        Vec3 vec1 = pos1.subtract(0.5d,0.5d,0.5d);
        Vec3 v = pos2.subtract(0.5d,0.5d,0.5d);
        //pos2 = player.getEyePosition().subtract(0.5,1,0.5);
        Vec3 vec2 = v.subtract(vec1);

        float yaw = (float) Math.atan2(vec2.x, vec2.z);
        float c = (float) Math.sqrt(vec2.x * vec2.x + vec2.z * vec2.z);
        float pitch = (float) Math.atan2(vec2.y, c);
        float length = (float) vec2.length();
        //

        //
        int segmentCount = (int) ((length * 16) / 4) + 1;
		double reciprocal = 1 / (double) segmentCount;
		DoubleUnaryOperator curveFunction   = x -> 0.2f * x * (x * reciprocal - 1);
		DoubleUnaryOperator curveDerivative = x -> 0.2f * (2 * x * reciprocal - 1);

		if ((vec1.distanceTo(player.getEyePosition()) > cableRenderDistance) && (v.distanceTo(player.getEyePosition()) > cableRenderDistance))
			return;
			
		for (int i = 0; i < segmentCount; i++) {
			float angle = (float) Math.toDegrees(Math.atan(curveDerivative.applyAsDouble(i)));
			ms.pushPose();
			
			CachedBuffers.partial(model, air)
				.translate(cameraInverse)
				.translate(vec1)
				.light(600)
				.translate(0.5f, 0.5f, 0.5f)
				.rotate(yaw, Direction.Axis.Y)
				.rotate(-pitch, Direction.Axis.X)
				.translateY((float) curveFunction.applyAsDouble(i) * 0.25f)
				.translate(0, 0, i * (4 / 16f))
				.rotateDegrees(-angle, Direction.Axis.X)
				.translate(-0.5f, -0.5f, -0.5f)
				.renderInto(ms, vc);
		}
    }

    public static DoubleUnaryOperator derive(DoubleUnaryOperator f, double h) {
        return x -> (f.applyAsDouble(x + h) - f.applyAsDouble(x - h)) / (2 * h);
    }


    @OnlyIn(Dist.CLIENT)
    public static void tickOutlines() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || !(mc.hitResult instanceof BlockHitResult result))
            return;

        ClientLevel level = mc.level;
        BlockPos pos = result.getBlockPos();
        Player player = mc.player;
        //    ItemStack heldItem = player.getMainHandItem();

        if (level.getBlockEntity(pos) instanceof IRealisticElectric be) {
            List<ConnectingElectricalNode> connectors = new ArrayList<>();
            ElectricalProperties properties = be.getProperties();

            RealElectricalNetwork network = RealElectricNetworkManager.getNetwork(level);

            List<ElectricalNode> nodes = network.getNodes(pos);

            for (ElectricalNode node : nodes) {
                if (node instanceof ConnectingElectricalNode connector)
                    connectors.add(connector);
            }

            for (ConnectingElectricalNode node : connectors) {
				Vec3 cablePos = node.getPosition().add(Vec3.atLowerCornerOf(be.getPos()));

                Vec3 corner1 = cablePos.add(0.1f, 0.1f, 0.1f);
                Vec3 corner2 = cablePos.subtract(0.1f, 0.1f, 0.1f);

                TFMGUtils.createOutline(corner1, corner2, "connector_" + node.localId, Color.rainbowColor(AnimationTickHolder.getTicks() * 5));
            }

        }

    }

}
