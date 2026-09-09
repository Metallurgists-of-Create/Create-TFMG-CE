package com.drmangotea.tfmg.content.electricity.experimental.simulation;

import com.drmangotea.tfmg.content.electricity.experimental.IRealisticElectric;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class ConnectingElectricalNode extends ElectricalNode {

    public Vec3 position;

    public ConnectingElectricalNode(BlockPos pos, int networkId, int localId, Vec3 position) {
        super(pos, networkId, localId);
        this.position = position;
    }

    public Vec3 getPosition() {
        return position;
    }

}
