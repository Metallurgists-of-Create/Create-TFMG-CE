package com.drmangotea.tfmg.content.machinery.oil_processing.pumpjack;

import com.drmangotea.tfmg.content.machinery.oil_processing.pumpjack.base.PumpjackBaseBlockEntity;
import com.drmangotea.tfmg.content.machinery.oil_processing.pumpjack.crank.PumpjackCrankBlockEntity;
import com.drmangotea.tfmg.content.machinery.oil_processing.pumpjack.hammer.PumpjackBlockEntity;
import com.drmangotea.tfmg.registry.TFMGTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import javax.annotation.Nullable;

public class PumpjackMultiblock {
	private final PumpjackBlockEntity master;
	private @Nullable BlockPos crankPos;
	private @Nullable PumpjackCrankBlockEntity crank;
	private @Nullable BlockPos basePos;
	private @Nullable PumpjackBaseBlockEntity base;
	private @Nullable BlockPos headPos;
	private @Nullable BlockPos connectorPos;
	
	public int connectorDistance = 0;
	public int headDistance = 0;
	public boolean connectorAtFront = false;
	public boolean headAtFront = false;
	public int crankConnectorDistance = 0;
	public int headBaseDistance = 0;
	
	public PumpjackMultiblock(PumpjackBlockEntity master) {
		this.master = master;
	}
	
	private @Nullable BlockEntity getBE (BlockPos pos) {
		if (master.getLevel() != null && master.getLevel().getBlockEntity(pos) instanceof BlockEntity be)
			return be;
		return null;
	}
	
	public boolean setCrank(@Nullable BlockPos pos) {
		if (pos != null && getBE(pos) instanceof PumpjackCrankBlockEntity crankBE) {
			this.crank = crankBE;
			this.crankPos = pos;
			return true;
		} else {
			this.crank = null;
			this.crankPos = null;
			return false;
		}
	}
	
	public boolean setBase(@Nullable BlockPos pos) {
		if (pos != null && getBE(pos) instanceof PumpjackBaseBlockEntity baseBE) {
			this.base = baseBE;
			this.basePos = pos;
			return true;
		} else {
			this.base = null;
			this.basePos = null;
			return false;
		}
	}
	
	public PumpjackBlockEntity getMaster() {
		return master;
	}
	
	@Nullable
	public BlockPos getCrank() {
		return this.crankPos;
	}
	
	@Nullable
	public PumpjackCrankBlockEntity getCrankBE() {
		return this.crank;
	}
	
	@Nullable
	public BlockPos getBase() {
		return this.basePos;
	}
	
	@Nullable
	public PumpjackBaseBlockEntity getBaseBE() {
		return this.base;
	}
	
	@Nullable
	public BlockPos getHead() {
		return this.headPos;
	}
	
	@Nullable
	public BlockPos getConnector() {
		return this.connectorPos;
	}
	
	public boolean isHead(BlockPos pos) {
		return master.getLevel() != null & master.getLevel().getBlockState(pos).is(TFMGTags.Blocks.PUMPJACK_HEAD.tag);
	}
	
	public boolean isPart(BlockPos pos) {
		return master.getLevel() != null & master.getLevel().getBlockState(pos).is(TFMGTags.Blocks.PUMPJACK_PART.tag);
	}
	
	public boolean isConnector(BlockPos pos) {
		return master.getLevel() != null & master.getLevel().getBlockState(pos).is(TFMGTags.Blocks.PUMPJACK_CONNECTOR.tag);
	}
	
	public boolean correctFacing(BlockPos pos) {
		if (master.getLevel() == null) return false;
		
		BlockState other = master.getLevel().getBlockState(pos);
		if (!other.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) return false;
		
		return other.getValue(BlockStateProperties.HORIZONTAL_FACING).getAxis() ==
			master.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING).getAxis();
	}
	
	public void evaluate() {
		Direction direction = master.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
		Direction.Axis masterAxis = direction.getAxis();
		BlockPos pos = master.getBlockPos();
		if (connectorPos != null) {
			connectorDistance = Math.abs(pos.get(masterAxis) - connectorPos.get(masterAxis));
			
			if (crank != null) {
				crankConnectorDistance = Math.abs(crank.getBlockPos().getY() - connectorPos.getY());
				crank.crankRadius = (float) connectorDistance / 5;
			}
		}
		if (headPos != null) {
			headDistance = Math.abs(pos.get(masterAxis) - headPos.get(masterAxis));
			if (base != null) {
				headBaseDistance = Math.abs(base.getBlockPos().getY() - headPos.getY());
			}
		}
		if (connectorPos != null) findCrank();
		if (crank != null && !(getBE(crankPos) instanceof PumpjackCrankBlockEntity))
			crank = null;
		if (headPos != null) findBase();
		if (base != null && !(getBE(basePos) instanceof PumpjackBaseBlockEntity))
				base = null;
	}
	
	private void findCrank() {
		if (connectorPos == null) return;
		for (int i = 0; i < 7; i++) {
			if (setCrank(connectorPos.below(i))) break;
		}
	}
	
	private void findBase() {
		if (headPos == null) return;
		for (int i = 0; i < 8; i++) {
			if (setBase(headPos.below(i))) break;
		}
	}
	
	public boolean isComplete() {
		return base != null && crank != null;
	}
	
	public void findHeadAndConnector() {
		if (master.getLevel() == null) return;
		Direction direction = master.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
		connectorPos = null;
		headPos = null;
		for (int i = 1; i < 7; i++) {
			BlockPos checkedPos = master.getBlockPos().above().relative(direction, i);
			if (connectorPos != null && headPos != null) return;
			if (isHead(checkedPos)) {
				headPos = checkedPos;
				headAtFront = true;
				continue;
			}
			boolean correct = correctFacing(checkedPos);
			if (isConnector(checkedPos) && correct) {
				connectorPos = checkedPos;
				connectorAtFront = true;
				continue;
			}
			if (!isPart(checkedPos)) {
				break;
			} else if (!correct) {
				break;
			}
		}
		for (int i = 1; i < 7; i++) {
			BlockPos checkedPos = master.getBlockPos().above().relative(direction.getOpposite(), i);
			if (connectorPos != null && headPos != null) return;
			if (isHead(checkedPos)) {
				headPos = checkedPos;
				headAtFront = false;
				continue;
			}
			boolean correct = correctFacing(checkedPos);
			if (isConnector(checkedPos) && correct) {
				connectorPos = checkedPos;
				connectorAtFront = false;
				continue;
			}
			if (!isPart(checkedPos)) {
				break;
			} else if (!correct) {
				break;
			}
		}
	}
	
	public void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
		NbtUtils.readBlockPos(compound, "Crank").ifPresent(this::setCrank);
		NbtUtils.readBlockPos(compound, "Base").ifPresent(this::setBase);
		NbtUtils.readBlockPos(compound, "Head").ifPresent(p -> headPos = p);
		NbtUtils.readBlockPos(compound, "Connector").ifPresent(p -> connectorPos = p);
	}
	
	public CompoundTag write(HolderLookup.Provider registries, boolean clientPacket) {
		CompoundTag compound = new CompoundTag();
		if (crankPos != null) {
			compound.put("Crank", NbtUtils.writeBlockPos(crankPos));
		}
		if (this.basePos != null) {
			compound.put("Base", NbtUtils.writeBlockPos(basePos));
		}
		if (this.headPos != null) {
			compound.put("Head", NbtUtils.writeBlockPos(headPos));
		}
		if (this.connectorPos != null) {
			compound.put("Connector", NbtUtils.writeBlockPos(connectorPos));
		}
		return compound;
	}
}
