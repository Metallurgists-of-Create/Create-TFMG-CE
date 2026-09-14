package com.drmangotea.tfmg.integration.jade.providers;

import com.drmangotea.tfmg.content.electricity.connection.cables.CableConnectorBlock;
import com.drmangotea.tfmg.integration.jade.TFMGJadePlugin;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum CableConnectorProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (accessor.getServerData().contains("InputMode")) {
            tooltip.add(Component.translatable("block.tfmg.cable_connector.input_mode").append(
                    Component.translatable("block.tfmg.cable_connector.input_mode." +  accessor.getServerData().getBoolean("InputMode")).withStyle(ChatFormatting.GOLD)));
        }
    }


    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        data.putBoolean("InputMode", accessor.getBlockState().getValue(CableConnectorBlock.INPUT_MODE));
    }

    @Override
    public ResourceLocation getUid() {
        return TFMGJadePlugin.CABLE_CONNECTOR;
    }
}
