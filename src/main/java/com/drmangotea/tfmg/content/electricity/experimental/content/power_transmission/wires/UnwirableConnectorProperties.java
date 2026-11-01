package com.drmangotea.tfmg.content.electricity.experimental.content.power_transmission.wires;

import com.drmangotea.tfmg.content.electricity.experimental.ElectricalProperties;
import com.drmangotea.tfmg.content.electricity.experimental.simulation.nodes.DirectionalElectricalNode;
import com.drmangotea.tfmg.content.electricity.experimental.simulation.nodes.NodeType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.List;

public class UnwirableConnectorProperties extends ElectricalProperties {
    public UnwirableConnectorProperties(BlockPos pos, List<Direction> directions) {
        super(pos);

        nodes.add(new DirectionalElectricalNode(position,0,directions, NodeType.CONNECTOR));
    }



    @Override
    public int getId() {
        return 15;
    }




}
