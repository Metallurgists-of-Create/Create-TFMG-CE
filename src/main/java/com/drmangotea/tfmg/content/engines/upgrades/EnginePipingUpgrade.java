package com.drmangotea.tfmg.content.engines.upgrades;


import com.drmangotea.tfmg.content.engines.types.AbstractSmallEngineBlockEntity;
import com.drmangotea.tfmg.registry.TFMGBlocks;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import java.util.Optional;

public class EnginePipingUpgrade extends EngineUpgrade {
    @Nullable
    public FluidTankBlockEntity tank = null;

    public void findTank(AbstractSmallEngineBlockEntity be) {
        Level level = be.getLevel();
        if (level == null) return;
        for (Direction direction : Direction.values()) {
            BlockPos pos = be.getBlockPos().relative(direction);
            if (level.getBlockEntity(pos) instanceof FluidTankBlockEntity foundTank) {
                tank = foundTank;
                return;
            }
        }
        tank = null;
    }

    @Override
    public void updateUpgrade(AbstractSmallEngineBlockEntity be) {
        findTank(be);
    }

    @Override
    public void lazyTickUpgrade(AbstractSmallEngineBlockEntity engine) {
        if (tank != null) {
            AbstractSmallEngineBlockEntity controller = engine.getControllerBE();

            int maxOutput = tank.getTankInventory().drain(500, IFluidHandler.FluidAction.SIMULATE).getAmount();
            int maxInput = tank.getTankInventory().fill(new FluidStack(tank.getFluid(0).getFluidHolder(), 500), IFluidHandler.FluidAction.SIMULATE);
            if(controller == null)
                return;
            if(controller.fuelTank == null)
                return;

            int amount = Math.min(maxInput, Math.min(maxOutput, controller.fuelTank.getSpace()));
            tank.getTankInventory().drain(amount, IFluidHandler.FluidAction.EXECUTE);
            controller.getControllerBE().fuelTank.fill(new FluidStack(tank.getFluid(0).getFluidHolder(), amount), IFluidHandler.FluidAction.EXECUTE);
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
