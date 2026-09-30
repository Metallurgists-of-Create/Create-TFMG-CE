package com.drmangotea.tfmg.content.electricity.experimental.content.power_transmission.wires;

import com.drmangotea.tfmg.content.electricity.experimental.ElectricalProperties;
import com.drmangotea.tfmg.content.electricity.experimental.simulation.nodes.ConnectingElectricalNode;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

public class ConnectorProperties extends ElectricalProperties {
    public ConnectorProperties(BlockPos pos) {
        super(pos);

        nodes.add(new ConnectingElectricalNode(position,0, new Vec3(0.5d, 0.5d, 0.5d)));
    }


    @Override
    public int getId() {
        return 2;
    }

    public CompoundTag saveData(CompoundTag compound) {
            return compound;
    }
}
