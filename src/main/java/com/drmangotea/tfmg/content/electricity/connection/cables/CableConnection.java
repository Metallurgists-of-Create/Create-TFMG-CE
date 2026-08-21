package com.drmangotea.tfmg.content.electricity.connection.cables;

import com.drmangotea.tfmg.base.TFMGUtils;
import com.drmangotea.tfmg.content.electricity.connection.cable_type.CableType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record CableConnection(BlockPos pos1, BlockPos pos2, CableType type, boolean visible) {
	public CompoundTag saveConnection() {
		CompoundTag compoundTag = new CompoundTag();
		
		compoundTag.put("Pos", NbtUtils.writeBlockPos(pos1));
		compoundTag.put("Pos2", NbtUtils.writeBlockPos(pos2));
		
		compoundTag.putBoolean("Visible", visible);
		compoundTag.putString("CableType", type.getKey().toString());
		
		return compoundTag;
	}
	
	public static CableConnection loadConnection(CompoundTag compoundTag) {
		BlockPos pos1 = NbtUtils.readBlockPos(compoundTag, "Pos").get();
		BlockPos pos2 = NbtUtils.readBlockPos(compoundTag, "Pos2").get();
		
		boolean visible = compoundTag.getBoolean("Visible");
		CableType type = TFMGUtils.getCableType(ResourceLocation.parse(compoundTag.getString("CableType")));
		return new CableConnection(pos1, pos2, type, visible);
	}
	
	public float getLength() {
		return TFMGUtils.getDistance(pos1, pos2, false);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof CableConnection c) {
			return (pos1.equals(c.pos1) && pos2.equals(c.pos2))
				|| (pos1.equals(c.pos2) && pos2.equals(c.pos1));
		} //this is only used for checking whether a new connection should be made,
		//  so we only care if a connection already exists between the two points.
		return false;
	}
	
	@Override @NotNull
	public String toString() { // for Debug purposes
		return "CableConnection{" +
			pos1 +
			" -{" + type.getKey() + "}-> " +
			pos2 +
			", visible=" + visible +
			'}';
	}
}