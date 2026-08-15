package com.drmangotea.tfmg.content.machinery.oil_processing.surface_scanner;

import com.drmangotea.tfmg.config.TFMGConfigs;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import javax.annotation.Nullable;

public class SurfaceScannerSable {
    @Nullable
    private static SurfaceScannerBlockEntity cachedScanner;
    private static long cachedTick = Long.MIN_VALUE;
    @Nullable
    private static SubLevelAccess cachedSubLevel;

    //Crazy?
    //I was crazy once.
    //They locked me in a room,
    //A rubber room,
    //A rubber room with math,
    //The math made me crazy...
    public static BlockPos evaluateOilPos(SurfaceScannerBlockEntity scanner, int x, int z) {
        BlockPos actualPosition = getActualPosition(scanner);
        Quaterniond rot = SurfaceScannerSable.getSublevelRot(scanner);
        int scanDepth = TFMGConfigs.common().machines.surfaceScannerScanDepth.get();
        int cx = x - 2;
        int cz = z - 2;
        Vector3d chunkOffset = new Vector3d(cx, 0.0, cz);

        if (rot != null) rot.transform(chunkOffset);

        int rx = (int) Math.round(chunkOffset.x);
        int rz = (int) Math.round(chunkOffset.z);

        if (Math.abs(Math.abs(chunkOffset.x) - Math.abs(chunkOffset.z)) < 0.5) {
            int mag = (int) Math.round((Math.abs(chunkOffset.x) + Math.abs(chunkOffset.z)) / 2.0);
            rx = (int) Math.copySign(mag, chunkOffset.x);
            rz = (int) Math.copySign(mag, chunkOffset.z);
        }

        return new BlockPos(
                actualPosition.getX() + rx * 16,
                scanDepth,
                actualPosition.getZ() + rz * 16
        );
    }

    public static BlockPos getActualPosition(SurfaceScannerBlockEntity scanner) {
        BlockPos scannerPos = scanner.getBlockPos();
        SubLevelAccess subLevel = getScannerSubLevel(scanner);
        if (subLevel == null) {
            return scannerPos;
        }
        Vec3 transformed = subLevel.logicalPose().transformPosition(scannerPos.getCenter());
        return BlockPos.containing(transformed.x, transformed.y, transformed.z);
    }

    public static Quaterniond getSublevelRot(SurfaceScannerBlockEntity scanner) {
        SubLevelAccess subLevel = getScannerSubLevel(scanner);
        Quaterniond rot = new Quaterniond();
        if (subLevel != null) {
            rot = new Quaterniond(subLevel.logicalPose().orientation());
        }
        return rot;
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
