package com.drmangotea.tfmg.content.electricity.experimental.content.lights;

import com.drmangotea.tfmg.content.electricity.experimental.ElectricalProperties;
import com.drmangotea.tfmg.content.electricity.experimental.simulation.Resistance;
import com.drmangotea.tfmg.content.electricity.experimental.simulation.nodes.ConnectableElectricalNode;
import com.drmangotea.tfmg.content.electricity.experimental.simulation.nodes.ElectricalNode;
import net.minecraft.core.BlockPos;

public class LightProperties extends ElectricalProperties {
    public LightProperties(BlockPos pos) {
        super(pos);
        ElectricalNode N = new ConnectableElectricalNode(position, 0);
        ElectricalNode L1 = new ConnectableElectricalNode(position, 1);
        nodes.add(N);
        nodes.add(L1);
        components.add(new Resistance(N,L1,400,0,position));
    }

    @Override
    public boolean needsUpdateData() {
        return true;
    }

    @Override
    public int getId() {
        return 7;
    }

    @Override
    public boolean cableConnectable() {
        return true;
    }
}
