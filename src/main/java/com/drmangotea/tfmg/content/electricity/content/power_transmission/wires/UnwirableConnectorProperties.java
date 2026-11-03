package com.drmangotea.tfmg.content.electricity.content.power_transmission.wires;

import com.drmangotea.tfmg.content.electricity.ElectricalProperties;
import com.drmangotea.tfmg.content.electricity.simulation.nodes.DirectionalElectricalNode;
import com.drmangotea.tfmg.content.electricity.simulation.nodes.NodeType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.List;

public class UnwirableConnectorProperties extends ElectricalProperties {
    public UnwirableConnectorProperties(long pos1, List<Direction> directions) {
        super(pos1);

        BlockPos pos = BlockPos.of(position);
        nodes.add(new DirectionalElectricalNode(position,0,directions, NodeType.CONNECTOR));
    }



    @Override
    public int getId() {
        return 15;
    }




}
