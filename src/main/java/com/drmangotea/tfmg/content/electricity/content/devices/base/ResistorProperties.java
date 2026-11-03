package com.drmangotea.tfmg.content.electricity.content.devices.base;

import com.drmangotea.tfmg.content.electricity.CablePos;
import com.drmangotea.tfmg.content.electricity.content.DirectionalElectricalProperties;
import com.drmangotea.tfmg.content.electricity.simulation.nodes.ConnectingElectricalNode;
import com.drmangotea.tfmg.content.electricity.simulation.Resistance;
import com.drmangotea.tfmg.content.electricity.simulation.nodes.NodeType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;

public class ResistorProperties extends DirectionalElectricalProperties {

    public ResistorProperties(long pos, Direction direction) {
        this(pos,direction,1000,NodeType.PHASE_1);
    }

    public ResistorProperties(long pos, Direction direction,double r,NodeType type) {
        super(pos,direction);
        BlockPos pos1 = BlockPos.of(position);
        ConnectingElectricalNode node1 = new ConnectingElectricalNode(position, 0, getRotation(this.direction).getFirst(), NodeType.NEUTRAL);
        ConnectingElectricalNode node2 = new ConnectingElectricalNode(position, 1, getRotation(this.direction).get(1),type);
        nodes.add(node1);
        nodes.add(node2);
        components.add(new Resistance(node1, node2, r, 0,position));

    }

    public List<CablePos> getRotation(Direction direction) {

        List<CablePos> positions = new ArrayList<>();

        switch (direction) {
            case DOWN,UP -> {
                positions.add(new CablePos(0.5f,1,0.5f));
                positions.add(new CablePos(0.5f,0,0.5f));
            }
            case WEST,EAST -> {
                positions.add(new CablePos(0.5f,0.5f,1));
                positions.add(new CablePos(0.5f,0.5f,0));
            }
            case NORTH,SOUTH -> {
                positions.add(new CablePos(1,0.5f,0.5f));
                positions.add(new CablePos(0,0.5f,0.5f));
            }


        };

        return positions;
    }

    @Override
    public List<Direction> getConnectableDirections() {
        return List.of(Direction.values());
    }

    @Override
    public boolean cableConnectable() {
        return true;
    }

    @Override
    public boolean needsUpdateData() {
        return true;
    }

    @Override
    public int getId() {
        return 3;
    }
}
