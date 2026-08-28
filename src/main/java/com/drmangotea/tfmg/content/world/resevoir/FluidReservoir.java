package com.drmangotea.tfmg.content.world.resevoir;

import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;

import java.util.ArrayList;
import java.util.List;

public class FluidReservoir {
    public final long id;
    private int oilReserves;
    public List<BlockPos> deposits = new ArrayList<>();

    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("Id", this.id);
        tag.putInt("OilReserves", this.oilReserves);
        tag.put("Deposits", NBTHelper.writeCompoundList(this.deposits, p -> {
            CompoundTag nbt = new CompoundTag();
            nbt.put("Pos", NbtUtils.writeBlockPos(p));
            return nbt;
        }));
        return tag;
    }

    public static FluidReservoir read(CompoundTag tag) {
        long id = tag.getLong("Id");
        FluidReservoir reservoir = new FluidReservoir(id);
        reservoir.setReserves(tag.getInt("OilReserves"));
        NBTHelper.iterateCompoundList(tag.getList("Deposits", Tag.TAG_COMPOUND), nbt -> reservoir.deposits.add(NBTHelper.readBlockPos(nbt, "Pos")));
        return reservoir;
    }

    public FluidReservoir(BlockPos origin) {
        this.id = origin.asLong();
        deposits.add(origin);
    }

    protected FluidReservoir(long id) {
        this.id = id;
        this.oilReserves = 0;
        this.deposits = new ArrayList<>();
    }

    public long getId() {
        return this.id;
    }

    public boolean isEmpty() {
        return this.oilReserves <= 0;
    }

    public void setReserves(int reserves) {
        this.oilReserves = reserves;
    }

    public int getReserves() {
        return this.oilReserves;
    }

    public void drain(int amount) {
        if (this.oilReserves > 0) {
            this.oilReserves -= Math.min(amount, this.oilReserves);
        }
    }

    public int depositAmount() {
        return this.deposits.size();
    }

    public List<BlockPos> getDeposits() {
        return new ArrayList<>(this.deposits);
    }
}
