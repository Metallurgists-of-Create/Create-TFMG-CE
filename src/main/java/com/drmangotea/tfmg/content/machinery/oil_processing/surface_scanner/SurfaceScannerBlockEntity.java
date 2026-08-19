package com.drmangotea.tfmg.content.machinery.oil_processing.surface_scanner;

import com.drmangotea.tfmg.TFMG;
import com.drmangotea.tfmg.base.TFMGUtils;
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

import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;

public class SurfaceScannerBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    private long lastScanTick = Long.MIN_VALUE;
    private BlockPos lastScanPos = null;
	private Quaterniond lastScanRot = new Quaterniond();
	private BlockPos nearestDeposit;

    public boolean[][] grid = new boolean[5][5];

    public SurfaceScannerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setLazyTickRate(20);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

    public void findDeposits() {
        if (level == null) return;
        if (!level.isClientSide) return;
		
		BlockPos actualPosition = SurfaceScannerSable.getActualPosition(this);
		int scanDepth = TFMGConfigs.common().machines.surfaceScannerScanDepth.get();

        for (int x = 0; x < 5; x++) { for (int z = 0; z < 5; z++) {
			BlockPos pos = new BlockPos(
				actualPosition.getX() + (x - 2) * 16,
				scanDepth,
				actualPosition.getZ() + (z - 2) * 16
			);
			boolean oil = hasOil(pos);
			grid[x][z] = oil;
			if (oil) {
				if (nearestDeposit == null) {
					nearestDeposit = pos;
				} else {
					float currentDistance = TFMGUtils.getDistance(actualPosition, nearestDeposit, true);
					float newDistance = TFMGUtils.getDistance(actualPosition, pos, true);
					if (newDistance < currentDistance) nearestDeposit = pos;
				}
			}
		} }
		
		level.updateNeighborsAt(getBlockPos(), getBlockState().getBlock());
    }
	
	public boolean operational() {
		return level != null
			&& level.getBlockEntity(getBlockPos().below()) instanceof MachineInputBlockEntity input
			&& Math.abs(input.getSpeed()) >= TFMGConfigs.common().machines.surfaceScannerMinimumRPM.get();
	}

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        TFMGTexts.header("surface_scanner")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        if (operational()) {
            int depositsFound = 0;
            for (boolean[] row : grid) { for (boolean light : row) {
				if (light) depositsFound++;
			} }
            if (depositsFound > 0) {
                TFMGTexts.SurfaceScanner.deposits(depositsFound).forGoggles(tooltip);
				//add nearest deposit tooltip?
            } else {
				TFMGTexts.SurfaceScanner.noDeposit().forGoggles(tooltip);
            }
        } else {
			TFMGTexts.CommonMachines.minRPM(TFMGConfigs.common().machines.surfaceScannerMinimumRPM.get())
				.style(ChatFormatting.DARK_RED).forGoggles(tooltip);
        }
        return true;
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (level == null) return;
        BlockPos actualPosition = SurfaceScannerSable.getActualPosition(this);
        if (operational()) {
            boolean moved = lastScanPos == null || !lastScanPos.equals(actualPosition);
            Quaterniond currentRot = SurfaceScannerSable.getSublevelRot(this);
            boolean rotChanged = !currentRot.equals(lastScanRot);
            int intervalTicks = 2400;
            long currentTick = level != null ? level.getGameTime() : Long.MIN_VALUE;
            boolean intervalElapsed = lastScanTick == Long.MIN_VALUE || (currentTick - lastScanTick) >= intervalTicks;

            if (!moved && !rotChanged && !intervalElapsed) {
                return;
            }

            findDeposits();
            lastScanPos = actualPosition;
            lastScanTick = currentTick;
            lastScanRot = currentRot;
        } else {
            grid = new boolean[5][5];
			nearestDeposit = null;
        }
    }

    public boolean hasOil(BlockPos pos) {
        if (level == null) return false;
        ChunkAccess chunk = level.getChunk(pos);
        AABB checkedArea = new AABB(chunk.getPos().getMiddleBlockPosition(TFMGConfigs.common().machines.surfaceScannerScanDepth.get()).north().west());
        checkedArea = checkedArea.inflate(7,0,7);
        for (BlockState state : chunk.getBlockStates(checkedArea).toList()) {
            if(state.is(TFMGTags.Blocks.SURFACE_SCANNER_FINDABLE.tag))
				return true;
        }
        return false;
    }

    public int getDirectionalSignal (Direction side) {
		if (nearestDeposit == null) return 0;
		//normalized direction vector:
		Vec3 direction = switch (side) {
			case DOWN, UP -> null;
			case NORTH -> new Vec3(0, 0,  1);
			case SOUTH -> new Vec3(0, 0, -1);
			case WEST -> new Vec3( 1, 0,  0);
			case EAST -> new Vec3(-1, 0,  0);
		};
		if (direction == null) return 0; //just in case
		//direction rotated to sublevel orientation:
		direction = TFMGUtils.rotateQuat(direction, lastScanRot.conjugate());
		
		Vec3 toNearest = Vec3.atCenterOf(lastScanPos).subtract(Vec3.atCenterOf(nearestDeposit));
		//2d distance:
		double dist = Math.sqrt(toNearest.x()*toNearest.x() + toNearest.z()*toNearest.z());
		if (dist <= 2) return 0;
		
		//normalized vector towards nearest deposit:
		toNearest =  new Vec3(toNearest.x() / dist, 0, toNearest.z() / dist);
		
		//cosine of the angle can be given by the dot product, since both are normalized
		double cosine = toNearest.dot(direction);
		return (int) Math.max(0, 30 * Math.asin(cosine) / Math.PI); //how Aero does it
	}
}