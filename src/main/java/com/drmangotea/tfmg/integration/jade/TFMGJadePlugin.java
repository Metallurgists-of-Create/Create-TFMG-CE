package com.drmangotea.tfmg.integration.jade;

import com.drmangotea.tfmg.TFMG;
import com.drmangotea.tfmg.content.electricity.connection.cables.CableConnectorBlock;
import com.drmangotea.tfmg.integration.jade.providers.CableConnectorProvider;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

import javax.annotation.ParametersAreNonnullByDefault;

@WailaPlugin
@ParametersAreNonnullByDefault
public class TFMGJadePlugin implements IWailaPlugin {
    public static final ResourceLocation CABLE_CONNECTOR = TFMG.asResource("cable_connector");

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(CableConnectorProvider.INSTANCE, CableConnectorBlock.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(CableConnectorProvider.INSTANCE, CableConnectorBlock.class);
    }
}
