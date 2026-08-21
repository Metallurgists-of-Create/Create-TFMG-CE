package com.drmangotea.tfmg.content.machinery.oil_processing.surface_scanner;

import com.drmangotea.tfmg.base.TFMGShapes;
import com.drmangotea.tfmg.registry.TFMGBlockEntities;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

public class SurfaceScannerBlock extends Block implements IBE<SurfaceScannerBlockEntity> {
    public SurfaceScannerBlock(Properties p) {
        super(p);
    }

    @Override
    public Class<SurfaceScannerBlockEntity> getBlockEntityClass() {
        return SurfaceScannerBlockEntity.class;
    }

    @Override @NotNull @ParametersAreNonnullByDefault
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return TFMGShapes.SLAB;
    }
	
	//TODO: Make the redstone work
	@Override @ParametersAreNonnullByDefault
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override @ParametersAreNonnullByDefault
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        final SurfaceScannerBlockEntity be = this.getBlockEntity(level, pos);
        if (be == null || side.getAxis().isVertical())
            return 0;
        return be.getDirectionalSignal(side);
    }
	
	@Override @ParametersAreNonnullByDefault
	public boolean shouldCheckWeakPower(final BlockState state, final SignalGetter level, final BlockPos pos, final Direction side) {
		return false;
	}
	
	@Override @ParametersAreNonnullByDefault
	public boolean canConnectRedstone(final BlockState state, final BlockGetter level, final BlockPos pos, @Nullable final Direction direction) {
		return !(direction == null || direction.getAxis().isVertical());
	}
	
	@Override
    public BlockEntityType<? extends SurfaceScannerBlockEntity> getBlockEntityType() {
        return TFMGBlockEntities.SURFACE_SCANNER.get();
    }
}
