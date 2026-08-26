package com.drmangotea.tfmg.config;


import com.drmangotea.tfmg.config.common.DepositConfig;
import com.drmangotea.tfmg.config.common.EquipmentConfig;
import com.drmangotea.tfmg.config.common.MachineConfig;
import net.createmod.catnip.config.ConfigBase;

public class TFMGCommonConfig extends ConfigBase {

    public final MachineConfig machines = nested(0, MachineConfig::new, "Config options for TFMG's machinery");
    public final DepositConfig worldgen = nested(1, DepositConfig::new, "Worldgen Settings");
    public final EquipmentConfig equipment = nested(2, EquipmentConfig::new, "Config options for TFMG's equipment");

    @Override
    public String getName() {
        return "common";
    }


}
