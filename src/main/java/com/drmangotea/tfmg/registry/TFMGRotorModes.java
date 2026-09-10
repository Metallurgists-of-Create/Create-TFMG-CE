package com.drmangotea.tfmg.registry;

import com.drmangotea.tfmg.content.machinery.vat.industrial_rotor.IndustrialRotorModels;
import com.drmangotea.tfmg.content.machinery.vat.industrial_rotor.mode.RotorMode;
import com.drmangotea.tfmg.content.machinery.vat.industrial_rotor.mode.RotorModeEntry;
import static com.drmangotea.tfmg.TFMG.REGISTRATE;

public class TFMGRotorModes {
    public static final RotorModeEntry<RotorMode> NONE = REGISTRATE.rotorMode("none", RotorMode::new)
            .properties(p -> p)
            .register();

    public static final RotorModeEntry<RotorMode> CRYSTAL_PULLER = REGISTRATE.rotorMode("crystal_puller", RotorMode::new)
            .properties(p -> p.operation(TFMGVatOperations.CRYSTAL_PULLER).partial(IndustrialRotorModels::getCrystalPuller))
            .register();

    public static void init() {}
}
