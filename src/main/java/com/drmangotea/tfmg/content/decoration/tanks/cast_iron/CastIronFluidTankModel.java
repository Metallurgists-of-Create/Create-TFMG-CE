package com.drmangotea.tfmg.content.decoration.tanks.cast_iron;


import com.drmangotea.tfmg.base.TFMGSpriteShifts;
import com.drmangotea.tfmg.content.decoration.tanks.TFMGFluidTankModel;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.fluids.tank.FluidTankCTBehaviour;
import com.simibubi.create.foundation.block.connected.CTModel;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import net.createmod.catnip.data.Iterate;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CastIronFluidTankModel extends TFMGFluidTankModel {

    protected static final ModelProperty<CullData> CULL_PROPERTY = new ModelProperty<>();

    public static CastIronFluidTankModel standard(BakedModel originalModel) {
        return new CastIronFluidTankModel(originalModel, TFMGSpriteShifts.CAST_IRON_FLUID_TANK, TFMGSpriteShifts.CAST_IRON_FLUID_TANK_TOP,
                TFMGSpriteShifts.CAST_IRON_FLUID_TANK_INNER);
    }


    private CastIronFluidTankModel(BakedModel originalModel, CTSpriteShiftEntry side, CTSpriteShiftEntry top,
                                   CTSpriteShiftEntry inner) {
        super(originalModel, side, top, inner);
    }
}