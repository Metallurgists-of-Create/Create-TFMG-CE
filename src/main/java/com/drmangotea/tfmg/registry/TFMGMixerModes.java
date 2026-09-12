package com.drmangotea.tfmg.registry;

import com.drmangotea.tfmg.content.machinery.vat.industrial_mixer.IndustrialMixerModels;
import com.drmangotea.tfmg.content.machinery.vat.industrial_mixer.mode.MixerMode;
import com.drmangotea.tfmg.content.machinery.vat.industrial_mixer.mode.MixerModeEntry;

import static com.drmangotea.tfmg.TFMG.REGISTRATE;

public class TFMGMixerModes {
    /**
     * Use {@link TFMGMixerModes#NONE} instead
     */
    @Deprecated(since = "1.2.5", forRemoval = true)
    public static final MixerModeEntry<MixerMode> none = TFMGMixerModes.NONE;

    /**
     * Use {@link TFMGMixerModes#MIXING} instead
     */
    @Deprecated(since = "1.2.5", forRemoval = true)
    public static final MixerModeEntry<MixerMode> mixing = TFMGMixerModes.MIXING;

    /**
     * Use {@link TFMGMixerModes#CENTRIFUGE} instead
     */
    @Deprecated(since = "1.2.5", forRemoval = true)
    public static final MixerModeEntry<MixerMode> centrifuge = TFMGMixerModes.CENTRIFUGE;

    public static final MixerModeEntry<MixerMode> NONE = REGISTRATE.mixerMode("none", MixerMode::new)
            .properties((p) -> p)
            .register();

    public static final MixerModeEntry<MixerMode> MIXING = REGISTRATE.mixerMode("mixing", MixerMode::new)
            .properties((p) -> p.operation(TFMGVatOperations.MIXING).partial(IndustrialMixerModels::getMixerModel))
            .register();

    public static final MixerModeEntry<MixerMode> CENTRIFUGE = REGISTRATE.mixerMode("centrifuge", MixerMode::new)
            .properties((p) -> p.operation(TFMGVatOperations.CENTRIFUGE).partial(IndustrialMixerModels::getCentrifugeModel))
            .register();

    public static void init() { }
}
