package com.drmangotea.tfmg.content.machinery.oil_processing.surface_scanner;

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
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;

import java.util.List;

public class SurfaceScannerBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    private long lastScanTick = Long.MIN_VALUE;
    private ChunkPos lastScanPos = null;
	private BlockPos nearestDeposit = null;
	private int[] signals = new int[4];
	
    public boolean[][] grid = new boolean[5][5];

    public SurfaceScannerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setLazyTickRate(20);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

    public void findDeposits() {
        if (level == null) return;

		BlockPos actualPosition = SurfaceScannerSable.getActualPosition(this);
		int scanDepth = TFMGConfigs.common().machines.surfaceScannerScanDepth.get();
		ChunkPos chunkPos = level.getChunk(actualPosition).getPos();
		
        for (int x = 0; x < 5; x++) { for (int z = 0; z < 5; z++) {
			ChunkAccess chunk = level.getChunk(chunkPos.x + x - 2, chunkPos.z + z - 2);
			BlockPos midpoint = chunk.getPos().getMiddleBlockPosition(scanDepth).north().west();
			boolean oil = hasOil(chunk, midpoint);
			grid[x][z] = oil;
			if (oil) {
				if (nearestDeposit == null) {
					nearestDeposit = midpoint;
				} else {
					if (nearestDeposit.equals(midpoint)) continue;
					float currentDistance = TFMGUtils.getDistance(actualPosition, nearestDeposit, true);
					float newDistance = TFMGUtils.getDistance(actualPosition, midpoint, true);
					if (newDistance < currentDistance) nearestDeposit = midpoint;
				}
			}
		} }
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
            for (boolean[] row : grid) {
                for (boolean light : row) {
                    if (light) depositsFound++;
                }
            }
            if (depositsFound > 0) {
                TFMGTexts.SurfaceScanner.deposits(depositsFound).forGoggles(tooltip);
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
        if (operational()) {
			BlockPos actualPosition = SurfaceScannerSable.getActualPosition(this);
			ChunkPos actualChunkPos = level.getChunk(actualPosition).getPos();
			Quaterniond currentRot = SurfaceScannerSable.getSublevelRot(this);
			recalculateSignals(actualPosition, currentRot);
			setChanged();
			boolean moved = lastScanPos == null || !lastScanPos.equals(actualChunkPos);
            long currentTick = level != null ? level.getGameTime() : Long.MIN_VALUE;
            boolean intervalElapsed = lastScanTick == Long.MIN_VALUE || (currentTick - lastScanTick) >= 2400;

            if (!moved && !intervalElapsed) return;

            findDeposits();
            lastScanPos = actualChunkPos;
            lastScanTick = currentTick;
        } else {
            grid = new boolean[5][5];
			nearestDeposit = null;
        }
    }

    public boolean hasOil (ChunkAccess chunk, BlockPos midpoint) {
        if (level == null) return false;
        AABB checkedArea = new AABB(midpoint).inflate(7,0,7);
        for (BlockState state : chunk.getBlockStates(checkedArea).toList()) {
            if(state.is(TFMGTags.Blocks.SURFACE_SCANNER_FINDABLE.tag))
				return true;
        }
        return false;
    }
	
	public int getDirectionalSignal (Direction side) {
		if (!operational()) return 0;
		return switch (side) {
			case DOWN, UP -> 0;
			case NORTH -> signals[0];
			case SOUTH -> signals[1];
			case WEST -> signals[2];
			case EAST -> signals[3];
		};
	}
	
	private void recalculateSignals (BlockPos actualPosition, Quaterniond rot) {
		if (nearestDeposit == null) {
			signals[0] = 0; signals[1] = 0; signals[2] = 0; signals[3] = 0;
			return;
		}
		
		Vec3 toNearest = Vec3.atCenterOf(actualPosition).subtract(Vec3.atCenterOf(nearestDeposit));
		//2d distance:
		double dist = Math.sqrt(toNearest.x()*toNearest.x() + toNearest.z()*toNearest.z());
		//normalized vector towards nearest deposit:
		toNearest =  new Vec3(toNearest.x() / dist, 0, toNearest.z() / dist);
		
		signals[0] = getSignalForSide(Direction.NORTH, toNearest, rot);
		signals[1] = getSignalForSide(Direction.SOUTH, toNearest, rot);
		signals[2] = getSignalForSide(Direction.WEST, toNearest, rot);
		signals[3] = getSignalForSide(Direction.EAST, toNearest, rot);
		
		setChanged();
	}
	
	private int getSignalForSide (Direction side, Vec3 toNearest, Quaterniond rot) {
		//normalized direction vector, rotated to sublevel orientation:
		Vec3 direction = TFMGUtils.rotateQuat(Vec3.atLowerCornerOf(side.getNormal()), rot);
		//cosine of the angle can be given by the dot product, since both are normalized
		double cosine = toNearest.dot(direction);
		//how Aero does it
		return (int) Math.max(0, 30 * Math.asin(cosine) / Math.PI);
	}
}
