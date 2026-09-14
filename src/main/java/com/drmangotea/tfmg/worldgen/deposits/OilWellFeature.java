package com.drmangotea.tfmg.worldgen.deposits;


import com.drmangotea.tfmg.content.world.resevoir.FluidReservoir;
import com.drmangotea.tfmg.registry.TFMGBlocks;
import com.drmangotea.tfmg.registry.TFMGDataAttachments;
import com.drmangotea.tfmg.registry.TFMGFluids;
import com.mojang.serialization.Codec;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.Tags;

public class OilWellFeature extends Feature<NoneFeatureConfiguration> {
    public OilWellFeature(Codec<NoneFeatureConfiguration> pCodec) {
        super(pCodec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        BlockPos startingPos = context.origin();
        WorldGenLevel level = context.level();
        BlockPos pos = startingPos;
        RandomSource randomsource = context.random();


        setBlock(level, startingPos, TFMGBlocks.OIL_DEPOSIT.getDefaultState());
        //resevoir handling
        ChunkAccess chunk = level.getChunk(startingPos);
        if (chunk.hasData(TFMGDataAttachments.FLUID_RESERVOIR)) {
            FluidReservoir.addToReservoir(chunk, startingPos);
        } else {
            FluidReservoir.createReservoir(chunk, startingPos, level.getRandom());
        }

        int height = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, pos.getX(), pos.getZ()) + 70 + randomsource.nextInt(12);

        for (int i = 0; i < height; i++) {
            pos = pos.above();
            for (Direction direction : Iterate.directions) {
                if (randomsource.nextInt(3) == 1) {
                    if (direction.getAxis().isHorizontal() && level.getBlockState(pos.relative(direction)).is(Blocks.STONE)) {
                        setBlock(level, pos.relative(direction), TFMGBlocks.FOSSILSTONE.getDefaultState());
                    }
                }
            }
            if (i == height - 18) {
                AABB area = new AABB(pos).inflate(10);
                for (BlockPos oilPos : BlockPos.betweenClosed(new BlockPos((int) area.minX, (int) area.minY, (int) area.minZ), new BlockPos((int) area.maxX, (int) area.maxY, (int) area.maxZ))) {
                    if (randomsource.nextInt(10) == 7){
                        if (level.getFluidState(oilPos).is(Fluids.WATER) || level.getBlockState(oilPos).is(Tags.Blocks.SANDS)) {
                            setBlock(level, oilPos, TFMGFluids.CRUDE_OIL.getSource().getSource(true).createLegacyBlock());
                            if (level.getBlockState(oilPos).is(Tags.Blocks.SANDS))
                                setBlock(level, oilPos, level.getBlockState(oilPos).updateShape(Direction.NORTH, level.getBlockState(oilPos), level, oilPos, oilPos));
                        }
                    }
                }
            }

            setBlock(level, pos, TFMGFluids.CRUDE_OIL.getSource().getSource(true).createLegacyBlock());
            level.getBlockState(pos).updateShape(Direction.NORTH,level.getBlockState(pos),level,pos,pos);
        }
        return true;
    }

    public static void setBlock(WorldGenLevel level, BlockPos pos, BlockState state){
        level.setBlock(pos,state,2);
    }
}
