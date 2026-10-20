package com.drmangotea.tfmg.content.electricity.experimental.simulation.nodes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Direction;

import java.util.List;

public class ConnectableElectricalNode extends ElectricalNode {

    public Vec3 position = new Vec3(0,0,0);

    public List<Direction> facing;

    public final NodeType type;

    public ConnectableElectricalNode(BlockPos pos, int localId, NodeType type) {
        super(pos, localId);
        this.type = type;
    }

    public Vec3 getPosition() {
        return position;
    }
}
