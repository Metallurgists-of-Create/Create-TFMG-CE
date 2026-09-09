package com.drmangotea.tfmg.content.electricity.experimental.blocks;

import com.drmangotea.tfmg.content.electricity.experimental.ElectricalProperties;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public abstract class DirectionalElectricalProperties extends ElectricalProperties {

    public Direction direction;

    public DirectionalElectricalProperties(long pos, Direction direction) {
        super(pos);
        this.direction = direction;
    }

    public abstract List<Vec3> getRotation(Direction direction);
}
