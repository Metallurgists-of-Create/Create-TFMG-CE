package com.drmangotea.tfmg.config;

import net.createmod.catnip.config.ConfigBase;

public class TFMGClientConfig extends ConfigBase {
    public final ConfigBool enablePressureNeedleWobble = b(true, "enablePressureNeedleWobble", "Whether the pressure needle wobbles for the aesthetics of super high/low pressure");

    @Override
    public String getName() {
        return "client";
    }
}
