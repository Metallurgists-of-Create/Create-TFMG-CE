package com.drmangotea.tfmg.content.electricity.experimental.blocks;

import com.drmangotea.tfmg.content.electricity.experimental.ElectricalProperties;
import com.drmangotea.tfmg.content.electricity.experimental.IRealisticElectric;
import com.drmangotea.tfmg.content.electricity.experimental.simulation.ConnectingElectricalNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

public class ConnectorProperties extends ElectricalProperties {
    public ConnectorProperties(long pos1) {
        super(pos1);

        BlockPos pos = BlockPos.of(position);
        nodes.add(new ConnectingElectricalNode(position,0,0, new Vec3(0,1,0)));
    }
    


    @Override
    public int getId() {
        return 2;
    }

    public CompoundTag saveData(CompoundTag compound){
            return compound;
    }
}
