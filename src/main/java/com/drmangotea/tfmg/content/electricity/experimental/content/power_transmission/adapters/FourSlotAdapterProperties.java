package com.drmangotea.tfmg.content.electricity.experimental.content.power_transmission.adapters;

import com.drmangotea.tfmg.content.electricity.experimental.content.DirectionalElectricalProperties;
import com.drmangotea.tfmg.content.electricity.experimental.simulation.nodes.ConnectingElectricalNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class FourSlotAdapterProperties extends DirectionalElectricalProperties {

    public FourSlotAdapterProperties(BlockPos pos, Direction direction) {
        super(pos, direction);


        ConnectingElectricalNode N = new ConnectingElectricalNode(position, 0, getRotation(direction).get(0));
        ConnectingElectricalNode L1 = new ConnectingElectricalNode(position, 1, getRotation(direction).get(1));
        ConnectingElectricalNode L2 = new ConnectingElectricalNode(position, 2, getRotation(direction).get(2));
        ConnectingElectricalNode L3 = new ConnectingElectricalNode(position, 3, getRotation(direction).get(3));

        nodes.add(N);
        nodes.add(L1);
        nodes.add(L2);
        nodes.add(L3);


    }

    @Override
    public boolean cableConnectable() {
        return true;
    }

    @Override
    public int getId() {
        return 5;
    }

    @Override
    public List<Vec3> getRotation(Direction direction) {
        List<Vec3> positions = new ArrayList<>();

        switch (direction) {
            case DOWN -> {
                positions.add(new Vec3(8/16f, 12/16f, 10/16f));
                positions.add(new Vec3(11/16f, 12/16f, 7/16f));
                positions.add(new Vec3(8/16f, 12/16f, 7/16f));
                positions.add(new Vec3(5/16f, 12/16f, 7/16f));
            }
            case UP ->{
                positions.add(new Vec3(8/16f, 4/16f, 6/16f));
                positions.add(new Vec3(5/16f, 4/16f, 9/16f));
                positions.add(new Vec3(8/16f, 4/16f, 9/16f));
                positions.add(new Vec3(11/16f, 4/16f, 9/16f));
            }
            case NORTH ->{
                positions.add(new Vec3(8/16f, 6/16f, 12/16f));
                positions.add(new Vec3(5/16f, 9/16f, 12/16f));
                positions.add(new Vec3(8/16f, 9/16f, 12/16f));
                positions.add(new Vec3(11/16f, 9/16f, 12/16f));
            }
            case SOUTH ->{
                positions.add(new Vec3(8/16f, 6/16f,  4/16f));
                positions.add(new Vec3(11/16f, 9/16f, 4/16f));
                positions.add(new Vec3(8/16f, 9/16f, 4/16f));
                positions.add(new Vec3(5/16f, 9/16f, 4/16f));
            }
            case EAST ->{
                positions.add(new Vec3(4/16f, 6/16f, 8/16f));
                positions.add(new Vec3(4/16f, 9/16f, 11/16f));
                positions.add(new Vec3(4/16f, 9/16f, 8/16f));
                positions.add(new Vec3(4/16f, 9/16f, 5/16f));
            }
            case WEST ->{
                positions.add(new Vec3(12/16f, 6/16f, 8/16f));
                positions.add(new Vec3(12/16f, 9/16f, 5/16f));
                positions.add(new Vec3(12/16f, 9/16f, 8/16f));
                positions.add(new Vec3(12/16f, 9/16f, 11/16f));
            }
        }

        return positions;
    }
}
