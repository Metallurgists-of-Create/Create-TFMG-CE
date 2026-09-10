package com.drmangotea.tfmg.content.machinery.vat.industrial_rotor;

import com.drmangotea.tfmg.registry.TFMGPartialModels;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;

public class IndustrialRotorModels {
    public static PartialModel getCrystalPuller(int currentHeight, int totalHeight, IndustrialRotorBlockEntity be) {
        return TFMGPartialModels.VAT_SHAFT;
    }
}
