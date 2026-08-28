package com.drmangotea.tfmg.content.world.resevoir;

import com.drmangotea.tfmg.TFMG;
import com.drmangotea.tfmg.config.TFMGConfigs;
import com.drmangotea.tfmg.registry.TFMGBlocks;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FluidReservoirs extends SavedData {
    public static final String FILE_ID = TFMG.MOD_ID + "_deposits";

    private final List<FluidReservoir> reservoirs = new ArrayList<>();


    public FluidReservoir getReservoirFor(BlockPos pos) {
        for (FluidReservoir reservoir : reservoirs) {
            if (reservoir.deposits.contains(pos))
                return reservoir;
        }
        return null;
    }

    public void removeDeposit(BlockPos pos) {
        for (FluidReservoir reservoir : reservoirs) {
            if (reservoir.deposits.contains(pos)) {
                reservoir.deposits.remove(pos);
                if (reservoir.deposits.isEmpty())
                    reservoirs.remove(reservoir);
                return;
            }
        }
    }

    public void removeEmptyDeposits(Level level) {
        for (FluidReservoir reservoir : reservoirs) {
            if (reservoir.isEmpty()) {
                TFMG.LOGGER.debug("EPIC REMOVAL");
                reservoir.deposits.forEach((pos) -> level.setBlockAndUpdate(pos, Blocks.BEDROCK.defaultBlockState()));
                reservoirs.remove(reservoir);
                setDirty();
            }
        }
    }

    public boolean isReservoirNearby(BlockPos pos) {
        for (int x = -32; x < 32; x++) {
            for (int z = -32; z < 32; z++) {
                BlockPos checkedPos = pos.offset(x, 0, z);
                for (FluidReservoir reservoir : reservoirs) {
                    if (reservoir.id == checkedPos.asLong()) {
                        reservoir.deposits.add(pos);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void addDeposit(Level level, BlockPos pos) {
        if (!level.getBlockState(pos).is(TFMGBlocks.OIL_DEPOSIT.get()))
            return;
        if (containsDeposit(pos))
            return;

        for (FluidReservoir reservoir : reservoirs) {
            if (reservoir.id != pos.asLong()) {
                if (isReservoirNearby(pos)) {
                    return;
                }
            }
        }

        RandomSource randomSource = level.random;
        FluidReservoir reservoir = new FluidReservoir(pos);
        reservoir.setReserves(randomSource.nextInt(1000, TFMGConfigs.common().worldgen.depositMaxReserves.get()));
        if (!reservoir.deposits.isEmpty()) {
            reservoirs.add(reservoir);
            setDirty();
        }
    }

    public boolean containsDeposit(BlockPos pos) {
        for (FluidReservoir reservoir : reservoirs) {
            for(BlockPos deposit : reservoir.deposits){
                if(deposit.equals(pos))
                    return true;
            }
            if (reservoir.deposits.contains(pos)) {
                return true;
            }
        }
        return false;
    }

    public static FluidReservoirs load(CompoundTag compound) {
        FluidReservoirs sd = new FluidReservoirs();
        NBTHelper.iterateCompoundList(compound.getList("Reservoirs", Tag.TAG_COMPOUND), c -> {
            FluidReservoir reservoir = FluidReservoir.read(c);
            sd.reservoirs.add(reservoir);
        });
        return sd;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag compound, HolderLookup.Provider provider) {
        compound.put("Reservoirs", NBTHelper.writeCompoundList(reservoirs, FluidReservoir::write));
        return compound;
    }

    public static SavedData.Factory<FluidReservoirs> factory() {
        return new SavedData.Factory<>(FluidReservoirs::new, (compoundTag, provider) -> load(compoundTag));
    }

    public static FluidReservoirs get(ServerLevel serverLevel) {
        DimensionDataStorage storage = serverLevel.getDataStorage();
        return storage.computeIfAbsent(factory(), FILE_ID);
    }
}
