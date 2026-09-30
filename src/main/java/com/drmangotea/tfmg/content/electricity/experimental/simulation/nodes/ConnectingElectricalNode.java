package com.drmangotea.tfmg.content.electricity.experimental.simulation.nodes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class ConnectingElectricalNode extends ConnectableElectricalNode {
	
    public ConnectingElectricalNode(BlockPos pos, int localId, Vec3 position) {
        super(pos, localId);
        this.position = position;
    }

}
