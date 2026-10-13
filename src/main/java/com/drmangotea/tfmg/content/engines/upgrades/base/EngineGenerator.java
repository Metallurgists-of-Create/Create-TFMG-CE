package com.drmangotea.tfmg.content.engines.upgrades.base;

import com.drmangotea.tfmg.content.engines.types.AbstractSmallEngineBlockEntity;

public interface EngineGenerator {

    int getVoltageGeneration(AbstractSmallEngineBlockEntity engine);
    float getPowerGeneration(AbstractSmallEngineBlockEntity engine);
}
