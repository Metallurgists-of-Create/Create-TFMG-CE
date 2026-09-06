package com.drmangotea.tfmg.content.machinery.oil_processing.pumpjack.hammer;

import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.generators.ModelFile;

import static com.drmangotea.tfmg.content.machinery.oil_processing.pumpjack.hammer.PumpjackBlock.WIDE;
import static com.drmangotea.tfmg.content.machinery.oil_processing.pumpjack.hammer.PumpjackBlock.FACING;
import static com.simibubi.create.foundation.data.AssetLookup.partialBaseModel;

public class PumpjackGenerator extends SpecialBlockStateGen {
    @Override
    protected int getXRotation(BlockState state) {
        return 0;
    }

    @Override
    protected int getYRotation(BlockState state) {
        return (int) state.getValue(FACING).getClockWise().toYRot();
    }

    @Override
    public <T extends Block> ModelFile getModel(
		DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov, BlockState state
	) {
		return partialBaseModel(ctx, prov, state.getValue(WIDE) ? "wide" : "");
    }
}