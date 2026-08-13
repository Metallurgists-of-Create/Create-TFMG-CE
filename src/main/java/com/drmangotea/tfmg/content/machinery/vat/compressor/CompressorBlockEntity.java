package com.drmangotea.tfmg.content.machinery.vat.compressor;

import com.drmangotea.tfmg.base.lang.TFMGTexts;
import com.drmangotea.tfmg.config.TFMGConfigs;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Locale;

public class CompressorBlockEntity extends KineticBlockEntity {

    public CompressorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }



    public CompressorState getState() {
        if(Math.abs(getSpeed()) < TFMGConfigs.common().machines.compressorMinimumRPM.get()){
            return CompressorState.NOT_OPERATIONAL;
        }
        if(getSpeed()>0){
            return CompressorState.PRESSURIZING;
        }
        return CompressorState.DEPRESSURIZING;
    }


    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        TFMGTexts.CommonMachines.state("goggles." + getState().getSerializedName()).style(getState().color).forGoggles(tooltip);
        if(getState() == CompressorState.NOT_OPERATIONAL) {
            TFMGTexts.CommonMachines.minRPM(TFMGConfigs.common().machines.compressorMinimumRPM.get()).style(ChatFormatting.RED).forGoggles(tooltip);
        }
        return super.addToGoggleTooltip(tooltip, isPlayerSneaking);
    }

    public enum CompressorState implements StringRepresentable {
        PRESSURIZING(ChatFormatting.YELLOW),
        DEPRESSURIZING(ChatFormatting.AQUA),
        NOT_OPERATIONAL(ChatFormatting.RED);

        public final ChatFormatting color;

        CompressorState(ChatFormatting color) {
            this.color = color;
        }

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

}
