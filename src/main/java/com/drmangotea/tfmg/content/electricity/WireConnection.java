package com.drmangotea.tfmg.content.electricity;

import com.drmangotea.tfmg.content.electricity.simulation.nodes.ConnectableElectricalNode;
import net.minecraft.nbt.CompoundTag;

public record WireConnection (
	ConnectableElectricalNode node1,
	ConnectableElectricalNode node2,
	double resistance,
	boolean render
) {
	
	public CompoundTag save () {
		
		CompoundTag connectionTag = new CompoundTag();
		
		connectionTag.putDouble("Resistance", resistance);
		connectionTag.putBoolean("Should Render", render);
		connectionTag.put("Node1", node1.save());
		connectionTag.put("Node2", node2.save());
		
		return connectionTag;
	}
}
