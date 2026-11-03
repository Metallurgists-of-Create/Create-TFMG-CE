package com.drmangotea.tfmg.content.electricity.display_link.network;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.SingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;

public class NetConsumptionDisplaySource extends SingleLineDisplaySource {
    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        BlockEntity sourceBE = context.getSourceBlockEntity();
        if (!(sourceBE instanceof SmartBlockEntity smart))
            return EMPTY_LINE;
        //if (!(smart instanceof IOldElectric electric))
        //    return EMPTY_LINE;
//
        //float netUsage = electric.getNetworkPowerUsage();
//
        //if (smart.getBehaviour(ObservedElectricBehaviour.TYPE) != null) {
        //    IOldElectric observed = smart.getBehaviour(ObservedElectricBehaviour.TYPE).getObservedElectric();
        //    if (observed != null) netUsage = observed.getNetworkPowerUsage();
        //}
//
        //return Component.literal(TFMGUtils.formatUnits(netUsage, "W"));
        return Component.empty();
    }

    @Override
    protected String getTranslationKey() {
        return "electricity.network_consumption";
    }

    @Override
    protected boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }
}
