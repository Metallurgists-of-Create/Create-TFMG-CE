package com.drmangotea.tfmg.content.electricity.experimental.content.devices.electric_motor;

import com.drmangotea.tfmg.config.TFMGConfigs;
import com.drmangotea.tfmg.content.electricity.experimental.content.DirectionalElectricalProperties;
import com.drmangotea.tfmg.content.electricity.experimental.simulation.Resistance;
import com.drmangotea.tfmg.content.electricity.experimental.simulation.nodes.ConnectingElectricalNode;
import com.drmangotea.tfmg.content.electricity.experimental.simulation.nodes.ElectricalNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class ElectricMotorProperties extends DirectionalElectricalProperties {


    public ElectricMotorProperties(BlockPos pos1, Direction direction) {
        super(pos1, direction);
		List<Vec3> positions = getRotation(direction);

        ElectricalNode N  = new ConnectingElectricalNode(position, 0, positions.get(0));
        ElectricalNode L1 = new ConnectingElectricalNode(position, 1, positions.get(1));
        ElectricalNode L2 = new ConnectingElectricalNode(position, 2, positions.get(2));
        ElectricalNode L3 = new ConnectingElectricalNode(position, 3, positions.get(3));

        nodes.add(N);
        nodes.add(L1);
        nodes.add(L2);
        nodes.add(L3);

        components.add(new Resistance(L1, N, TFMGConfigs.common().machines.electricMotorInternalResistance.getF(), 0, position));
        components.add(new Resistance(L2, N, TFMGConfigs.common().machines.electricMotorInternalResistance.getF(), 1, position));
        components.add(new Resistance(L3, N, TFMGConfigs.common().machines.electricMotorInternalResistance.getF(), 2, position));
    }

    @Override
    public boolean needsUpdateData() {
        return true;
    }

    @Override
    public int getId() {
        return 8;
    }

    @Override
    public List<Vec3> getRotation(Direction direction) {
        List<Vec3> positions = new ArrayList<>();

        switch (direction) {
            case UP, DOWN, EAST, WEST, NORTH, SOUTH -> {
                positions.add(new Vec3(0, 7/16f,    3/16f));
                positions.add(new Vec3(0, 13 / 16f, 3 / 16f));
                positions.add(new Vec3(0, 13 / 16f, 8 / 16f));
                positions.add(new Vec3(0, 13 / 16f, 13 / 16f));
            }
        }

        return positions;
    }
}
