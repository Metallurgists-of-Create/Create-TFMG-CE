package com.drmangotea.tfmg.content.machinery.vat.industrial_rotor;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.function.Consumer;

import static com.simibubi.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING;

public class IndustrialRotorVisual extends KineticBlockEntityVisual<IndustrialRotorBlockEntity> {
    protected final RotatingInstance shaft;

    public IndustrialRotorVisual(VisualizationContext context, IndustrialRotorBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);

        shaft = instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT_HALF)).createInstance();
        Direction facing = blockEntity.getBlockState().getValue(HORIZONTAL_FACING);

        shaft.setup(blockEntity)
                .setPosition(getVisualPosition())
                .rotateToFace(Direction.SOUTH, facing)
                .setChanged();
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        consumer.accept(shaft);
    }

    @Override
    public void updateLight(float partialTick) {
        Direction facing = blockEntity.getBlockState().getValue(HORIZONTAL_FACING);
        BlockPos front = pos.relative(facing);
        relight(front, shaft);
    }

    @Override
    protected void _delete() {
        shaft.delete();
    }

    @Override
    public void update(float partialTick) {
        shaft.setup(blockEntity).setChanged();
    }
}
