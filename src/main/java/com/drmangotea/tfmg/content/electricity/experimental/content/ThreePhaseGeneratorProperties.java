package com.drmangotea.tfmg.content.electricity.experimental.content;

import com.drmangotea.tfmg.content.electricity.experimental.simulation.Resistance;
import com.drmangotea.tfmg.content.electricity.experimental.simulation.nodes.ConnectingElectricalNode;
import com.drmangotea.tfmg.content.electricity.experimental.simulation.nodes.ElectricalNode;
import com.drmangotea.tfmg.content.electricity.experimental.simulation.IdealVoltageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class ThreePhaseGeneratorProperties extends DirectionalElectricalProperties {
    public ThreePhaseGeneratorProperties(BlockPos pos, Direction d) {
        super(pos,d);

        ElectricalNode N = new ConnectingElectricalNode(position, 0,  getRotation(direction).get(0));
        ElectricalNode L1 = new ConnectingElectricalNode(position, 1, getRotation(direction).get(1));
        ElectricalNode L2 = new ConnectingElectricalNode(position, 2, getRotation(direction).get(2));
        ElectricalNode L3 = new ConnectingElectricalNode(position, 3, getRotation(direction).get(3));

        ElectricalNode L1r = new ElectricalNode(position, 4);
        ElectricalNode L2r = new ElectricalNode(position, 5);
        ElectricalNode L3r = new ElectricalNode(position, 6);



        nodes.add(N);
        nodes.add(L1);
        nodes.add(L2);
        nodes.add(L3);
        nodes.add(L1r);
        nodes.add(L2r);
        nodes.add(L3r);
        components.add(new IdealVoltageSource(L1r, N, 100,1000, 0, 0));
        components.add(new IdealVoltageSource(L2r, N, 100,1000, 120, 1));
        components.add(new IdealVoltageSource(L3r, N, 100,1000, 240, 2));

        components.add(new Resistance(L1,L1r,5,0,position));
        components.add(new Resistance(L2,L2r,5,1,position));
        components.add(new Resistance(L3,L3r,5,2,position));
        //components.add(new Resistance(L1, N, 10));
        //components.add(new Resistance(L1, N, 10));

    }

    @Override
    public boolean needsUpdateData() {
        return true;
    }

    @Override
    public boolean cableConnectable() {
        return true;
    }



    @Override
    public int getId() {
        return 1;
    }
	
	@Override
    public List<Vec3> getRotation(Direction direction) {
        List<Vec3> positions = new ArrayList<>();

		positions.add(new Vec3(0,  7/16f,  3/16f));
		positions.add(new Vec3(0, 13/16f,  3/16f));
		positions.add(new Vec3(0, 13/16f,  8/16f));
		positions.add(new Vec3(0, 13/16f, 13/16f));

        return positions;
    }
}
