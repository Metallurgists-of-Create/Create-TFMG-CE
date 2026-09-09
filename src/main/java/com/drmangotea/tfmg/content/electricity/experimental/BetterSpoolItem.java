package com.drmangotea.tfmg.content.electricity.experimental;

import com.drmangotea.tfmg.TFMG;
import com.drmangotea.tfmg.base.TFMGUtils;
import com.drmangotea.tfmg.content.electricity.experimental.simulation.ConnectingElectricalNode;
import com.drmangotea.tfmg.content.electricity.experimental.simulation.ElectricalNode;
import com.drmangotea.tfmg.registry.TFMGDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class BetterSpoolItem extends Item {
    public BetterSpoolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Vec3 clickPosition = context.getClickLocation();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();

        if (!(level.getBlockEntity(pos) instanceof IRealisticElectric be))
			return InteractionResult.PASS;
		
		ConnectingElectricalNode node1 = closestNode(be, clickPosition);
		TFMG.LOGGER.debug("Closest node is " + node1.getNetworkId());
		BlockPos pos2 = stack.get(TFMGDataComponents.POSITION);
		if (pos2 == null) {
			stack.set(TFMGDataComponents.POSITION, pos);
			stack.set(TFMGDataComponents.CONNECTOR_ID, node1.getLocalId());
			TFMG.LOGGER.debug("Saved node " + node1.getLocalId() + " " + node1.getNetworkId());
		} else {
			RealElectricalNetwork network = RealElectricNetworkManager.getNetwork(level);

			int id = stack.getOrDefault(TFMGDataComponents.CONNECTOR_ID, 0);

			if (!(level.getBlockEntity(pos2) instanceof IRealisticElectric be2))
				return InteractionResult.PASS;
			
			List<ElectricalNode> nodes = be2.getProperties().nodes;
			
			for (ElectricalNode n : nodes) {
				if (n.getLocalId() == id && n instanceof ConnectingElectricalNode node2) {
					network.connections.add(new WireConnection(node1, node2, 10));
					network.update();
					TFMG.ELECTRICAL_NETWORK_DATA.markDirty();
					stack.remove(TFMGDataComponents.CONNECTOR_ID);
					stack.remove(TFMGDataComponents.POSITION);
					
					return InteractionResult.SUCCESS;
				}
			}
		}
		
		return InteractionResult.SUCCESS;
	}

    private ConnectingElectricalNode closestNode(IRealisticElectric be, Vec3 clickPosition) {
		BlockPos pos = be.getPos();
		AtomicReference<Float> closestDistance = new AtomicReference<>(1000f);
		AtomicReference<ConnectingElectricalNode> closestConnector = new AtomicReference<>((ConnectingElectricalNode) be.getProperties().nodes.getFirst());

        be.getProperties().nodes.forEach(n -> {
            if (!(n instanceof ConnectingElectricalNode node)) return;
			
			Vec3 position = node.getPosition().add(pos.getX(), pos.getY(), pos.getZ());
			float distance = TFMGUtils.getDistance(clickPosition, position);
            if (distance < closestDistance.get()) {
                closestDistance.set(distance);
                closestConnector.set(node);
            }
        });

        for (ElectricalNode node : RealElectricNetworkManager.getNetwork(be.getWorld()).getNodes(be.getPos())) {
            TFMG.LOGGER.debug("nodes are " + node.getNetworkId());
        }

        return closestConnector.get();
    }

}
