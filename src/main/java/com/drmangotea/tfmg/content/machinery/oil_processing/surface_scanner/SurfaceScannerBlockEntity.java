package com.drmangotea.tfmg.content.machinery.oil_processing.surface_scanner;

import com.drmangotea.tfmg.base.lang.TFMGTexts;
import com.drmangotea.tfmg.config.TFMGConfigs;
import com.drmangotea.tfmg.content.machinery.misc.machine_input.MachineInputBlockEntity;
import com.drmangotea.tfmg.integration.sable.SurfaceScannerSable;
import com.drmangotea.tfmg.registry.TFMGTags;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.AABB;

import java.util.List;
import org.joml.Quaterniond;

public class SurfaceScannerBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {

    private long lastScanTick = Long.MIN_VALUE;
    private BlockPos lastScanPos = null;
    private Quaterniond lastScanRot = new Quaterniond();

    public Boolean[][] grid = new Boolean[5][5];
    private final boolean[][] serverGrid = new boolean[5][5];

    public SurfaceScannerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setLazyTickRate(20);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

    public void findDeposits() {
        if (level == null) return;
        if (!level.isClientSide) return;

        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                grid[x][z] = hasOil(SurfaceScannerSable.evaluateOilPos(this, x, z));
            }
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        TFMGTexts.header("surface_scanner")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        if (level == null) return false;
        boolean operational = level.getBlockEntity(getBlockPos().below()) instanceof MachineInputBlockEntity be && Math.abs(be.getSpeed()) >= TFMGConfigs.common().machines.industrialMixerMinimumRPM.get();
        if(operational) {
            int depositsFound = 0;
            for(Boolean[] row : grid){
                for(Boolean light : row){
                    if(light != null && light)
                        depositsFound++;
                }
            }
            if(depositsFound > 0){
                TFMGTexts.SurfaceScanner.deposits(depositsFound).forGoggles(tooltip);
            }else
                TFMGTexts.SurfaceScanner.noDeposit().forGoggles(tooltip);
        } else
            TFMGTexts.CommonMachines.minRPM(TFMGConfigs.common().machines.surfaceScannerMinimumRPM.get()).style(ChatFormatting.DARK_RED).forGoggles(tooltip);
        return true;
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (level == null) return;
        BlockPos actualPosition = SurfaceScannerSable.getActualPosition(this);
        if (level.getBlockEntity(getBlockPos().below()) instanceof MachineInputBlockEntity input && Math.abs(input.getSpeed()) >= TFMGConfigs.common().machines.surfaceScannerMinimumRPM.get()) {
            boolean moved = lastScanPos == null || !lastScanPos.equals(actualPosition);
            Quaterniond currentRot = SurfaceScannerSable.getSublevelRot(this);
            boolean rotChanged = currentRot != lastScanRot;
            int intervalTicks = 2400;
            long currentTick = level != null ? level.getGameTime() : Long.MIN_VALUE;
            boolean intervalElapsed = lastScanTick == Long.MIN_VALUE || (currentTick - lastScanTick) >= intervalTicks;

            if (!moved && !rotChanged && !intervalElapsed) {
                return;
            }

            findDeposits();
            if (level != null && !level.isClientSide) {
                updateServerGrid();
            }
            lastScanPos = actualPosition;
            lastScanTick = currentTick;
            lastScanRot = currentRot;
        } else {
            grid = new Boolean[5][5];
        }
    }

    private void updateServerGrid() {
        if (level == null) return;
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                serverGrid[x][z] = hasOil(SurfaceScannerSable.evaluateOilPos(this, x, z));
            }
        }
        level.updateNeighborsAt(getBlockPos(), getBlockState().getBlock());
    }

    public boolean hasOil(BlockPos pos) {
        if (level == null) return false;
        ChunkAccess chunk = level.getChunk(pos);
        AABB checkedArea = new AABB(chunk.getPos().getMiddleBlockPosition(TFMGConfigs.common().machines.surfaceScannerScanDepth.get()).north().west());
        checkedArea = checkedArea.inflate(7,0,7);
        for(BlockState state : chunk.getBlockStates(checkedArea).toList()){
            if(state.is(TFMGTags.Blocks.SURFACE_SCANNER_FINDABLE.tag))
                return true;
        }
        return false;
    }

    public int getDirectionalSignal(Direction side) {
        int bestDistance = Integer.MAX_VALUE;
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                if (!serverGrid[x][z]) {
                    continue;
                }
                int dx = x - 2;
                int dz = z - 2;
                Direction cellDirection = dominantDirection(dx, dz);
                if (cellDirection != null && cellDirection != side) {
                    continue;
                }
                int distance = Math.max(Math.abs(dx), Math.abs(dz));
                bestDistance = Math.min(bestDistance, distance);
            }
        }
        if (bestDistance == Integer.MAX_VALUE) {
            return 0;
        }
        return Math.max(1, 15 - bestDistance * 5);
    }

    private Direction dominantDirection(int dx, int dz) {
        if (dx == 0 && dz == 0) {
            return null;
        }
        if (Math.abs(dx) >= Math.abs(dz)) {
            return dx > 0 ? Direction.WEST : Direction.EAST;
        }
        return dz > 0 ? Direction.NORTH : Direction.SOUTH;
    }
}
