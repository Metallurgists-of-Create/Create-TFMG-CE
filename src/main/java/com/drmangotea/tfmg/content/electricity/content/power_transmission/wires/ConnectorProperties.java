package com.drmangotea.tfmg.content.electricity.content.power_transmission.wires;

import com.drmangotea.tfmg.content.electricity.ElectricalProperties;
import com.drmangotea.tfmg.content.electricity.ElectricalPropertiesType;
import com.drmangotea.tfmg.content.electricity.simulation.nodes.ConnectingElectricalNode;
import com.drmangotea.tfmg.content.electricity.simulation.nodes.NodeType;
import com.drmangotea.tfmg.registry.TFMGElectricalProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

public class ConnectorProperties extends ElectricalProperties {
    public ConnectorProperties(BlockPos pos) {
        super(pos);

        nodes.add(new ConnectingElectricalNode(position,0, new Vec3(0.5d, 0.5d, 0.5d), NodeType.CONNECTOR));
    }

    @Override
    public ElectricalPropertiesType<?> getType() {
        return TFMGElectricalProperties.CONNECTOR.get();
    }

    public CompoundTag saveData(CompoundTag compound) {
            return compound;
    }
}
