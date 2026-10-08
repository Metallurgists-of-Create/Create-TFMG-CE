package com.drmangotea.tfmg.content.electricity.experimental.content.power_transmission.transformers.small;

import com.drmangotea.tfmg.content.electricity.experimental.content.DirectionalElectricalProperties;
import com.drmangotea.tfmg.content.electricity.experimental.simulation.Transformer;
import com.drmangotea.tfmg.content.electricity.experimental.simulation.nodes.ConnectingElectricalNode;
import com.drmangotea.tfmg.content.electricity.experimental.simulation.nodes.ElectricalNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class TransformerProperties extends DirectionalElectricalProperties {


    public TransformerProperties(BlockPos pos1, Direction direction) {
        super(pos1, direction);
		List<Vec3> positions = getRotation(direction);

        ElectricalNode A = new ConnectingElectricalNode(position, 0, positions.get(0));
        ElectricalNode B = new ConnectingElectricalNode(position, 1, positions.get(1));
        ElectricalNode a = new ConnectingElectricalNode(position, 2, positions.get(2));
        ElectricalNode b = new ConnectingElectricalNode(position, 3, positions.get(3));

        nodes.add(A);
        nodes.add(B);
        nodes.add(a);
        nodes.add(b);

        components.add(new Transformer(A,B,a,b,1,10,1,1));

    }

    @Override
    public boolean needsUpdateData() {
        return true;
    }

    @Override
    public int getId() {
        return 9;
    }

    @Override
    public List<Vec3> getRotation(Direction direction) {
        List<Vec3> positions = new ArrayList<>();

        switch (direction) {
            case UP, DOWN, EAST, WEST, NORTH, SOUTH -> {
                positions.add(new Vec3(1, 7 / 16f, 8 / 16f));
                positions.add(new Vec3(1, 7 / 16f, 13 / 16f));
                positions.add(new Vec3(0, 13 / 16f, 8 / 16f));
                positions.add(new Vec3(0, 13 / 16f, 13 / 16f));
            }
        }

        return positions;
    }
}
