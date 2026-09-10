package com.drmangotea.tfmg.content.machinery.vat.industrial_rotor.mode;

import com.drmangotea.tfmg.content.machinery.vat.industrial_rotor.IndustrialRotorBlockEntity;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;

public interface RotorPartial {
    PartialModel getPartial(int currentHeight, int totalHeight, IndustrialRotorBlockEntity rotor);
}
