package com.drmangotea.tfmg.content.electricity.experimental.content.power_transmission.transformers.large;

import com.drmangotea.tfmg.content.electricity.experimental.content.DirectionalElectricalProperties;
import com.drmangotea.tfmg.content.electricity.experimental.simulation.Transformer;
import com.drmangotea.tfmg.content.electricity.experimental.simulation.nodes.ConnectingElectricalNode;
import com.drmangotea.tfmg.content.electricity.experimental.simulation.nodes.ElectricalNode;
import com.drmangotea.tfmg.content.electricity.experimental.simulation.nodes.NodeType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class LargeTransformerProperties extends DirectionalElectricalProperties {


    public LargeTransformerProperties(BlockPos pos, Direction direction) {
        super(pos, direction);

		List<Vec3> positions = getRotation(direction);
        ElectricalNode N = new ConnectingElectricalNode(position, 0, positions.get(0), NodeType.NEUTRAL);
        ElectricalNode U = new ConnectingElectricalNode(position, 1, positions.get(1), NodeType.PHASE_1);
        ElectricalNode V = new ConnectingElectricalNode(position, 2, positions.get(2), NodeType.PHASE_2);
        ElectricalNode W = new ConnectingElectricalNode(position, 3, positions.get(3), NodeType.PHASE_3);
        ElectricalNode u = new ConnectingElectricalNode(position, 4, positions.get(4), NodeType.PHASE_1);
        ElectricalNode v = new ConnectingElectricalNode(position, 5, positions.get(5), NodeType.PHASE_2);
        ElectricalNode w = new ConnectingElectricalNode(position, 6, positions.get(6), NodeType.PHASE_3);
        ElectricalNode n = new ConnectingElectricalNode(position, 7, positions.get(7), NodeType.NEUTRAL);


        nodes.add(N);
        nodes.add(U);
        nodes.add(V);
        nodes.add(W);
        nodes.add(u);
        nodes.add(v);
        nodes.add(w);
        nodes.add(n);

        components.add(new Transformer(U,N,u,n,1,10,1,0,position));
        components.add(new Transformer(V,N,v,n,1,10,1,1,position));
        components.add(new Transformer(W,N,w,n,1,10,1,2,position));

    }

    @Override
    public boolean needsUpdateData() {
        return true;
    }

    @Override
    public int getId() {
        return 12;
    }

    @Override
    public List<Vec3> getRotation(Direction direction) {
        List<Vec3> positions = new ArrayList<>();

        switch (direction) {
            case UP, DOWN, EAST, WEST, NORTH, SOUTH -> {
                positions.add(new Vec3(1, 0 / 16f, 4 / 16f));
                positions.add(new Vec3(1, 7 / 16f, 4 / 16f));
                positions.add(new Vec3(1, 7 / 16f, 8 / 16f));
                positions.add(new Vec3(1, 7 / 16f, 12 / 16f));
                positions.add(new Vec3(0, 13 / 16f, 4 / 16f));
                positions.add(new Vec3(0, 13 / 16f, 8 / 16f));
                positions.add(new Vec3(0, 13 / 16f, 12 / 16f));
                positions.add(new Vec3(1, 16 / 16f, 4 / 16f));
            }
        }

        return positions;
    }
}
