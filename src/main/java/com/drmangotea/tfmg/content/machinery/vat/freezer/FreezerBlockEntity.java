package com.drmangotea.tfmg.content.machinery.vat.freezer;

import com.drmangotea.tfmg.base.lang.TFMGTexts;
import com.drmangotea.tfmg.config.TFMGConfigs;
import com.drmangotea.tfmg.content.electricity.base.ElectricBlockEntity;
import com.drmangotea.tfmg.content.machinery.vat.base.VatBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class FreezerBlockEntity extends ElectricBlockEntity  {



    public FreezerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public int getMaxVoltage() {
        return 20000;
    }

    @Override
    public int getMaxCurrent() {
        return 400;
    }

    @Override
    public boolean hasElectricitySlot(Direction direction) {
        return true;
    }

    public boolean isOperational(){
        return getCurrent() >= TFMGConfigs.common().machines.freezerMinimumCurrent.get() && canWork();
    }


    @Override
    public boolean makeMultimeterTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean operational = getCurrent() >= TFMGConfigs.common().machines.freezerMinimumCurrent.get();
        TFMGTexts.CommonMachines.state("goggles." + (operational ? "operational" : "not_operational")).style(operational ? ChatFormatting.GREEN : ChatFormatting.RED).forGoggles(tooltip);
        if (!operational)
            TFMGTexts.Multimeter.notEnoughCurrent(TFMGConfigs.common().machines.freezerMinimumCurrent.get()).forGoggles(tooltip);
        super.makeMultimeterTooltip(tooltip, isPlayerSneaking);
        return true;
    }


    @Override
    public float resistance() {
        return 75;
    }



    @Override
    public void onNetworkChanged(int oldVoltage, float oldPower) {
        super.onNetworkChanged(oldVoltage, oldPower);
        VatBlock.updateVatState(getBlockState(), level, getBlockPos().relative(Direction.DOWN));
    }




    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos()).setMinY(getBlockPos().getY() - 2);
    }





}
