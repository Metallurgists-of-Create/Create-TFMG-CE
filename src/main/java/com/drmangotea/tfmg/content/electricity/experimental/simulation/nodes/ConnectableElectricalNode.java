package com.drmangotea.tfmg.content.electricity.experimental.simulation.nodes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class ConnectableElectricalNode extends ElectricalNode {

    public Vec3 position = new Vec3(0,0,0);

    public ConnectableElectricalNode(BlockPos pos, int localId) {
        super(pos, localId);
    }

    public Vec3 getPosition() {
        return position;
    }
}
