package com.drmangotea.tfmg.content.engines.upgrades;


import com.drmangotea.tfmg.content.engines.types.AbstractSmallEngineBlockEntity;
import com.drmangotea.tfmg.registry.TFMGBlocks;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import java.util.Optional;

public class EnginePipingUpgrade extends EngineUpgrade {
    @Nullable
    public BlockPos fluidTankPosition = null;
    @Nullable
    public Direction handlerDirection = null;

    public void findTank(AbstractSmallEngineBlockEntity be) {
        Level level = be.getLevel();
        if (level == null) return;
        for (Direction direction : Direction.values()) {
            BlockPos pos = be.getBlockPos().relative(direction);
            if (level.getCapability(Capabilities.FluidHandler.BLOCK, pos, direction.getOpposite()) != null) {
                fluidTankPosition = pos;
                handlerDirection = direction.getOpposite();
                return;
            }
        }
        fluidTankPosition = null;
        handlerDirection = null;
    }

    @Override
    public void updateUpgrade(AbstractSmallEngineBlockEntity be) {
        findTank(be);
    }

    @Override
    public void lazyTickUpgrade(AbstractSmallEngineBlockEntity engine) {
        Level level = engine.getLevel();
        if (level == null) return;
        if (fluidTankPosition == null || handlerDirection == null) return;
        IFluidHandler fluidHandler = engine.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, fluidTankPosition, handlerDirection);
        if (fluidHandler != null) {
            AbstractSmallEngineBlockEntity controller = engine.getControllerBE();
            if(controller == null)
                return;
            if(controller.fuelTank == null)
                return;

            FluidStack toDrain = FluidStack.EMPTY;
            FluidStack contained = controller.fuelTank.getFluid();
            for (int i = 0; i < fluidHandler.getTanks(); i++) {
                FluidStack fluidStack = fluidHandler.getFluidInTank(i);
                if (fluidStack.isEmpty()) continue;
                if (!contained.isEmpty() && FluidStack.isSameFluidSameComponents(contained, fluidStack)) {
                    toDrain = fluidStack.copy();
                    break;
                }
                toDrain = fluidStack.copy();
            }

            int maxInput = controller.fuelTank.fill(toDrain.copyWithAmount(500), IFluidHandler.FluidAction.SIMULATE);
            int maxOutput = fluidHandler.drain(toDrain.copyWithAmount(maxInput), IFluidHandler.FluidAction.SIMULATE).getAmount();

            int amount = Math.min(maxInput, Math.min(maxOutput, controller.fuelTank.getSpace()));
            controller.getControllerBE().fuelTank.fill(toDrain.copyWithAmount(amount), IFluidHandler.FluidAction.EXECUTE);
            fluidHandler.drain(toDrain.copyWithAmount(amount), IFluidHandler.FluidAction.EXECUTE);
        } else findTank(engine);
    }

    @Override
    public Optional<? extends EngineUpgrade> createUpgrade() {
        return Optional.of(new EnginePipingUpgrade());
    }

    @Override
    public Item getItem() {
        return TFMGBlocks.INDUSTRIAL_PIPE.asItem();
    }
}
