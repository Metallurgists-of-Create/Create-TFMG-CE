package com.drmangotea.tfmg.content.machinery.vat.industrial_mixer;

import com.drmangotea.tfmg.registry.TFMGPartialModels;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;

public class IndustrialMixerModels {

    public static PartialModel getCentrifugeModel(int currentHeight, int totalHeight, IndustrialMixerBlockEntity be) {
        if (be.vatSize == 1) {
            if (totalHeight == 1)
                return TFMGPartialModels.SMALL_CENTRIFUGE_ALONE;
            if (currentHeight == 0)
                return TFMGPartialModels.SMALL_CENTRIFUGE_TOP;
            if (currentHeight == totalHeight)
                return TFMGPartialModels.SMALL_CENTRIFUGE_BOTTOM;
            return TFMGPartialModels.SMALL_CENTRIFUGE_MIDDLE;
        } else {
            if (totalHeight == 1)
                return TFMGPartialModels.LARGE_CENTRIFUGE_ALONE;

            if (currentHeight == 0)
                return TFMGPartialModels.LARGE_CENTRIFUGE_TOP;
            if (currentHeight == totalHeight)
                return TFMGPartialModels.LARGE_CENTRIFUGE_BOTTOM;
            return TFMGPartialModels.LARGE_CENTRIFUGE_MIDDLE;
        }
    }

    public static PartialModel getMixerModel(int i, int height, IndustrialMixerBlockEntity be) {
        if (i == height - 1) {
            if (be.vatSize > 1) {
                return TFMGPartialModels.MIXER;
            } else {
                return TFMGPartialModels.SMALL_MIXER;
            }
        } else {
            return TFMGPartialModels.MIXER_SHAFT;
        }
    }
}
