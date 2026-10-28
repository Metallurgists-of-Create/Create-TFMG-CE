package com.drmangotea.tfmg.content.electricity.experimental.content.power_transmission.adapters;

import com.drmangotea.tfmg.content.electricity.experimental.content.DirectionalElectricalProperties;
import com.drmangotea.tfmg.content.electricity.experimental.simulation.nodes.HybridConnectedNode;
import com.drmangotea.tfmg.content.electricity.experimental.simulation.nodes.NodeType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class FourSlotAdapterProperties extends DirectionalElectricalProperties {

    public FourSlotAdapterProperties(BlockPos pos, Direction direction) {
        super(pos, direction);
		List<Vec3> positions = getRotation(direction);

        HybridConnectedNode N  = new HybridConnectedNode(position, 0, direction, positions.get(0), NodeType.NEUTRAL);
        HybridConnectedNode L1 = new HybridConnectedNode(position, 1, direction, positions.get(1), NodeType.PHASE_1);
        HybridConnectedNode L2 = new HybridConnectedNode(position, 2, direction, positions.get(2), NodeType.PHASE_2);
        HybridConnectedNode L3 = new HybridConnectedNode(position, 3, direction, positions.get(3), NodeType.PHASE_3);

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
    public List<Direction> getConnectableDirections() {
        return List.of(Direction.values());
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
