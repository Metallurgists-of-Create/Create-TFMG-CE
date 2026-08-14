package com.drmangotea.tfmg.content.machinery.oil_processing.surface_scanner;

import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class SurfaceScannerSable {
    @Nullable
    private static SurfaceScannerBlockEntity cachedScanner;
    private static long cachedTick = Long.MIN_VALUE;
    @Nullable
    private static SubLevelAccess cachedSubLevel;

    public static BlockPos getActualPosition(SurfaceScannerBlockEntity scanner) {
        BlockPos scannerPos = scanner.getBlockPos();
        SubLevelAccess subLevel = getScannerSubLevel(scanner);
        if (subLevel == null) {
            return scannerPos;
        }
        Vec3 local = new Vec3(scannerPos.getX() + 0.5, scannerPos.getY() + 0.5, scannerPos.getZ() + 0.5);
        Vec3 transformed = subLevel.logicalPose().transformPosition(local);
        return BlockPos.containing(transformed.x, transformed.y, transformed.z);
    }

    @Nullable
    private static SubLevelAccess getScannerSubLevel(SurfaceScannerBlockEntity scanner) {
        Level level = scanner.getLevel();
        long tick = level != null ? level.getGameTime() : -1;
        if (scanner != cachedScanner || tick != cachedTick) {
            cachedScanner = scanner;
            cachedTick = tick;
            cachedSubLevel = SableCompanion.INSTANCE.getContaining(scanner);
        }
        return cachedSubLevel;
    }
}
