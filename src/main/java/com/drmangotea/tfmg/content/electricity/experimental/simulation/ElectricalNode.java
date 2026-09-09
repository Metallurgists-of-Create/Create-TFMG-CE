package com.drmangotea.tfmg.content.electricity.experimental.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;

public class ElectricalNode {
    public int networkId;
    public int localId;
    public BlockPos pos;

    public ElectricalNode(BlockPos pos, int networkId, int localId) {
        this.networkId = networkId;
        this.localId = localId;
        this.pos = pos;
    }

    public int getNetworkId() {
        return networkId;
    }

    public int getLocalId() {
        return localId;
    }
	
	public CompoundTag save() {
		CompoundTag tag = new CompoundTag();
		tag.put("position", NbtUtils.writeBlockPos(this.pos));
		tag.putInt("local id", this.getLocalId());
		return tag;
	}
}
