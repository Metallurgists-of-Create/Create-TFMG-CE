package com.drmangotea.tfmg.content.machinery.metallurgy.blast_furnace;

import com.drmangotea.tfmg.config.TFMGConfigs;
import com.drmangotea.tfmg.registry.TFMGBlocks;
import com.drmangotea.tfmg.registry.TFMGTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;

public class BlastFurnaceMultiblock {
    private final BlastFurnaceOutputBlockEntity master;
    private @Nullable BlockPos tuyerePos;
    private @Nullable BlastFurnaceHatchBlockEntity tuyere;
    private int size = 0;
    private boolean reinforced = false;

    public BlastFurnaceMultiblock(BlastFurnaceOutputBlockEntity master) {
        this.master = master;
    }

    public void setTuyere(@Nullable BlockPos pos) {
        if (pos == null) {
            this.tuyere = null;
        } else {
            reevaluateTuyere(pos);
        }
        this.tuyerePos = pos;
    }

    public void reevaluateTuyere(BlockPos pos) {
        if (master.getLevel() == null) return;
        BlockEntity potentialTuyere = master.getLevel().getBlockEntity(pos);
        if (potentialTuyere instanceof BlastFurnaceHatchBlockEntity hatch) {
            this.tuyere = hatch;
        } else {
            this.tuyere = null;
            setTuyere(null);
        }
    }

    void setSize(int size) {
        this.size = size;
    }

    void setReinforced(boolean reinforced) {
        this.reinforced = reinforced;
    }

    @Nullable
    public BlockPos getTuyere() {
        return this.tuyerePos;
    }

    @Nullable
    public BlastFurnaceHatchBlockEntity getTuyereBlockEntity() {
        return this.tuyere;
    }

    public int getSize() {
        return this.size;
    }

    public boolean isReinforced() {
        return this.reinforced;
    }

    public void evaluate() {
        BlockPos middlePos = master.getBlockPos().relative(master.getBlockState().getValue(FACING).getOpposite());
        setTuyere(null);
        setSize(0);
        if (master.getLevel() == null) return;
        if (!wallType(middlePos).valid()) {
            return;
        }

        setSize(0);
        int normalAmount = 0;
        int reinforcedAmount = 0;
        for (int i = 0; i < TFMGConfigs.common().machines.blastFurnaceMaxHeight.get(); i++) {
            BlockPos checkedPos = middlePos.above(i).east().south();
            for (int j = 0; j < 3; j++) {
                for (int y = 0; y < 3; y++) {
                    BlockType wall = wallType(checkedPos);
                    BlockType support = supportType(checkedPos);
                    if (checkedPos.getX() == middlePos.getX() ^ checkedPos.getZ() == middlePos.getZ()) {
                        if (!(i == 0 && master.getLevel().getBlockState(checkedPos).is(TFMGBlocks.BLAST_FURNACE_OUTPUT.get()))) {
                            if (!wall.valid()) {
                                setReinforced(normalAmount == 0 && reinforcedAmount > 0);
                                return;
                            } else {
                                if (!wall.reinforced()) {
                                    normalAmount++;
                                } else reinforcedAmount++;
                            }
                        }
                    } else if (checkedPos.getX() == middlePos.getX() && checkedPos.getZ() == middlePos.getZ()) {
                        if (!master.getLevel().getBlockState(checkedPos).isAir() && i != 0) {
                            setReinforced(normalAmount == 0 && reinforcedAmount > 0);
                            return;
                        }
                    } else if (!support.valid()) {
                        setReinforced(normalAmount == 0 && reinforcedAmount > 0);
                        return;
                    } else {
                        if (!support.reinforced()) {
                            normalAmount++;
                        } else reinforcedAmount++;
                    }
                    checkedPos = checkedPos.west();
                }
                checkedPos = checkedPos.north().east(3);
            }
            setSize(getSize() + 1);
        }
    }

    public BlockType wallType(BlockPos pos) {
        if (master.getLevel() == null) return BlockType.INVALID;
        BlockState state = master.getLevel().getBlockState(pos);

        if (state.is(TFMGBlocks.BLAST_FURNACE_HATCH.get())) {
            if (getTuyere() != null)
                return BlockType.INVALID;
            setTuyere(pos);
            return BlockType.REINFORCED;
        }
        return BlockType.forWall(state);
    }

    public BlockType supportType(BlockPos pos) {
        if (master.getLevel() == null) return BlockType.INVALID;
        BlockState state = master.getLevel().getBlockState(pos);
        return BlockType.forSupport(state);
    }

    protected void read(CompoundTag beData, String key, HolderLookup.Provider registries, boolean clientPacket) {
        CompoundTag compound = beData.getCompound(key);
        if (compound.contains("Tuyere")) {
            setTuyere(NbtUtils.readBlockPos(compound, "Tuyere").orElse(null));
        }
        setReinforced(compound.getBoolean("Reinforced"));
        setSize(compound.getInt("Size"));
    }

    public CompoundTag write(HolderLookup.Provider registries, boolean clientPacket) {
        CompoundTag compound = new CompoundTag();
        if (getTuyere() != null) {
            compound.put("Tuyere", NbtUtils.writeBlockPos(getTuyere()));
        }
        compound.putBoolean("Reinforced", isReinforced());
        compound.putInt("Size", getSize());
        return compound;
    }

    public enum BlockType {
        INVALID,
        VALID,
        REINFORCED;

        public boolean valid() {
            return this != INVALID;
        }

        public boolean reinforced() {
            return this == REINFORCED;
        }

        public static BlockType forWall(BlockState state) {
            if (state.is(TFMGTags.TFMGBlockTags.REINFORCED_BLAST_FURNACE_WALL.tag))
                return REINFORCED;
            if (state.is(TFMGTags.TFMGBlockTags.BLAST_FURNACE_WALL.tag))
                return VALID;
            return INVALID;
        }

        public static BlockType forSupport(BlockState state) {
            if (state.is(TFMGTags.TFMGBlockTags.REINFORCED_BLAST_FURNACE_SUPPORT.tag))
                return REINFORCED;
            if (state.is(TFMGTags.TFMGBlockTags.BLAST_FURNACE_SUPPORT.tag))
                return VALID;
            return INVALID;
        }
    }
}
