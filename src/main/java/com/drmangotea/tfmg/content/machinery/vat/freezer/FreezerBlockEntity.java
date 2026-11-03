package com.drmangotea.tfmg.content.machinery.vat.freezer;

import com.drmangotea.tfmg.base.lang.TFMGTexts;

import com.drmangotea.tfmg.content.electricity.content.devices.base.ResistiveLoadBlockEntity;
import com.drmangotea.tfmg.content.machinery.vat.base.VatBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class FreezerBlockEntity extends ResistiveLoadBlockEntity {



    public FreezerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }



    public boolean isOperational(){
        return current>3;
    }


    @Override
    public boolean makeMultimeterTooltip(List<Component> tooltip, boolean isPlayerSneaking) {

        super.makeMultimeterTooltip(tooltip, isPlayerSneaking);
        if (!isOperational())
            TFMGTexts.Multimeter.notEnoughCurrent(3).forGoggles(tooltip);

        return true;
    }



    @Override
    public void onUpdated() {
        super.onUpdated();
        VatBlock.updateVatState(getBlockState(), level, getBlockPos().relative(Direction.DOWN));
    }






    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos()).setMinY(getBlockPos().getY() - 2);
    }





}
