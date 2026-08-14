package com.drmangotea.tfmg.content.machinery.oil_processing.surface_scanner;

import com.drmangotea.tfmg.base.TFMGShapes;
import com.drmangotea.tfmg.registry.TFMGBlockEntities;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.concurrent.atomic.AtomicInteger;

public class SurfaceScannerBlock extends Block implements IBE<SurfaceScannerBlockEntity> {
    public SurfaceScannerBlock(Properties p_49795_) {
        super(p_49795_);
    }

    @Override
    public Class<SurfaceScannerBlockEntity> getBlockEntityClass() {
        return SurfaceScannerBlockEntity.class;
    }

    @Override
    public VoxelShape getShape(BlockState p_60555_, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
        return TFMGShapes.SLAB;
    }

    public boolean isSignalSource(BlockState state) {
        return true;
    }

    public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        if (side.getAxis().isVertical()) {
            return 0;
        }
        AtomicInteger signal = new AtomicInteger(0);
        withBlockEntityDo(blockAccess, pos, (scanner) -> signal.set(scanner.getDirectionalSignal(side)));
        return signal.get();
    }

    @Override
    public BlockEntityType<? extends SurfaceScannerBlockEntity> getBlockEntityType() {
        return TFMGBlockEntities.SURFACE_SCANNER.get();
    }
}
