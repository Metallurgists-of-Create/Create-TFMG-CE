package com.drmangotea.tfmg.worldgen.deposits;


import com.drmangotea.tfmg.content.world.resevoir.FluidReservoir;
import com.drmangotea.tfmg.registry.TFMGBlocks;
import com.drmangotea.tfmg.registry.TFMGDataAttachments;
import com.drmangotea.tfmg.registry.TFMGFluids;
import com.mojang.serialization.Codec;
import net.createmod.catnip.data.Iterate;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;


public class OilDepositFeature extends Feature<NoneFeatureConfiguration> {
    public OilDepositFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        BlockPos startingPos = context.origin();
        WorldGenLevel level = context.level();
        BlockPos pos = startingPos;
        RandomSource randomsource = context.random();

        if (randomsource.nextInt(20) != 0)
            return false;

        for (int i = 0; i < randomsource.nextInt(6) + 1; i++) {
            placeDeposit(pos, level, randomsource);
            pos = pos.north(randomsource.nextInt(40) - 20);
            pos = pos.west(randomsource.nextInt(40) - 20);
        }
        return true;
    }

    public void placeDeposit(BlockPos startingPos, WorldGenLevel level, RandomSource randomsource) {
        BlockPos pos = startingPos;
        setBlock(level, startingPos, TFMGBlocks.OIL_DEPOSIT.getDefaultState());
		//resevoir handling
		ChunkAccess chunk = level.getChunk(startingPos);
        if (chunk.hasData(TFMGDataAttachments.FLUID_RESERVOIR)) {
            FluidReservoir.addToReservoir(chunk, startingPos);
        } else {
            FluidReservoir.createReservoir(chunk, startingPos, level.getRandom());
        }

        int height = randomsource.nextIntBetweenInclusive(10, 25);
        for (int i = 0; i < height; i++) {
            pos = pos.above();

            if (level.getBlockState(pos).isAir()) continue;

            setBlock(level, pos, TFMGFluids.CRUDE_OIL.get().getSource().defaultFluidState().createLegacyBlock());


            Direction crudeBranchDir = Util.getRandom(Iterate.horizontalDirections, randomsource);
            BlockPos crudeBranch = pos.relative(crudeBranchDir);
            if (!level.getBlockState(crudeBranch).isAir()) {
                setBlock(level, crudeBranch, TFMGFluids.CRUDE_OIL.get().getSource().defaultFluidState().createLegacyBlock());
            }

            if (i < (height / 2)) {
                Direction fossilDir = Util.getRandom(Iterate.horizontalDirections, randomsource);
                BlockPos fossil = pos.relative(fossilDir);
                if (level.getBlockState(fossil).is(BlockTags.BASE_STONE_OVERWORLD)) {
                    setBlock(level, fossil, TFMGBlocks.FOSSILSTONE.getDefaultState());
                }
            }
        }
    }
    
    public static void setBlock(WorldGenLevel level,BlockPos pos, BlockState state){
        level.setBlock(pos,state,2);
    }
}
