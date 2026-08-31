    package com.drmangotea.tfmg.content.machinery.oil_processing.distillation_tower.controller;

import com.drmangotea.tfmg.base.TFMGUtils;
import com.drmangotea.tfmg.base.lang.TFMGTexts;
import com.drmangotea.tfmg.config.TFMGConfigs;
import com.drmangotea.tfmg.content.decoration.tanks.steel.SteelTankBlock;
import com.drmangotea.tfmg.content.decoration.tanks.steel.SteelTankBlockEntity;
import com.drmangotea.tfmg.content.machinery.oil_processing.distillation_tower.output.DistillationOutputBlockEntity;
import com.drmangotea.tfmg.mixin.accessor.FluidTankBlockEntityAccessor;
import com.drmangotea.tfmg.recipes.DistillationRecipe;
import com.drmangotea.tfmg.recipes.input.DistillationRecipeInput;
import com.drmangotea.tfmg.registry.TFMGBlockEntities;
import com.drmangotea.tfmg.registry.TFMGRecipeTypes;
import com.drmangotea.tfmg.registry.TFMGTags;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.fluid.SmartFluidTank;

import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.drmangotea.tfmg.content.machinery.oil_processing.distillation_tower.controller.DistillationControllerBlock.getFacing;

public class DistillationControllerBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {

    LerpedFloat angle = LerpedFloat.angular();

    protected IFluidHandler fluidCapability;
    public final FluidTank tank = new SmartFluidTank(8000, this::onFluidStackChanged);

    protected boolean updateCapability;

    private final RecipeManager.CachedCheck<DistillationRecipeInput, DistillationRecipe> quickCheck;

    public boolean refreshOutputs = false;
    public List<BlockPos> outputs = new ArrayList<>();

    public int untilNextProcess = TFMGConfigs.common().machines.distillationRecipeGapTicks.get();

    public DistillationControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        fluidCapability = tank;
        this.quickCheck = RecipeManager.createCheck(TFMGRecipeTypes.DISTILLATION.getType());
        refreshOutputs = true;
        updateCapability = false;
        refreshCapability();
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                TFMGBlockEntities.DISTILLATION_CONTROLLER.get(),
                (be, context) -> be.fluidCapability
        );
    }

    @Override
    public void remove() {
        super.remove();
        SteelTankBlock.updateTowerState(level, getBlockPos().relative(getFacing(getBlockState()).getOpposite()),false,false);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    @Override
    public void invalidate() {
        super.invalidate();
        invalidateCapabilities();
    }

    public void refreshCapability() {
        fluidCapability = tank;
        invalidateCapabilities();
    }

    public void manageDialRendering(){
        if (level.isClientSide) {
            angle.chase(180 * ((float) tank.getFluidAmount() / tank.getCapacity()), 0.2f, LerpedFloat.Chaser.EXP);
            angle.tickChaser();
        }
    }

    public void manageRecipe(SteelTankBlockEntity controllerBe) {
        if (outputs.isEmpty() || controllerBe.activeHeat == 0)
            return;

        RecipeHolder<DistillationRecipe> recipeholder;
        if (!tank.isEmpty()) {
            recipeholder = quickCheck.getRecipeFor(new DistillationRecipeInput(tank.getFluidInTank(0), outputs.size()), level).orElse(null);
        } else {
            recipeholder = null;
        }

        if(recipeholder == null) {
            return;
        }

        DistillationRecipe recipe = recipeholder.value();

        ///
        int toDrain = recipe.getInputFluid().amount();
        int maxOutput = tank.drain(toDrain, IFluidHandler.FluidAction.SIMULATE).getAmount();
        if (maxOutput < toDrain)
            return;

        if (recipe.getFluidResults().toArray().length != outputs.size())
            return;
        if (controllerBe.isController()) {
            if (controllerBe.getHeight() < outputs.size() * 2 || (((FluidTankBlockEntityAccessor) controllerBe).tfmg$getWidth() < 2 && outputs.size() > 3))
                return;
        }  else {
            if (controllerBe.getControllerBE() != null)
                if (controllerBe.getControllerBE().getHeight() < outputs.size() * 2 || ((FluidTankBlockEntityAccessor) controllerBe.getControllerBE()).tfmg$getWidth() < 2)
                    return;
        }
        for (DistillationOutputBlockEntity output : outputs.stream().map(this::getOutput).toList()) {
            if (output == null)
                continue;
            if (output.tank.getSpace() == 0 && output.mode.get() == DistillationOutputBlockEntity.DistillationOutputMode.KEEP_FLUID)
                return;
        }
        int numero = 0;
        for (DistillationOutputBlockEntity output : outputs.stream().map(this::getOutput).toList()) {
            if (output == null)
                continue;
            FluidStack fluidStack = recipe.getFluidResults().get(numero);
            FluidStack result = new FluidStack(fluidStack.getFluidHolder(), fluidStack.getAmount());
            if (fluidStack.isEmpty())
                break;
            if (output.tank.fill(result, IFluidHandler.FluidAction.SIMULATE) > output.tank.getCapacity() && output.mode.get() == DistillationOutputBlockEntity.DistillationOutputMode.KEEP_FLUID)
                break;
            output.tank.fill(result, IFluidHandler.FluidAction.EXECUTE);
            numero++;
        }
        tank.drain(toDrain, IFluidHandler.FluidAction.EXECUTE);
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null) return;

        if (updateCapability) {
            updateCapability = false;
            refreshCapability();
        }

        if (refreshOutputs) {
            refreshOutputs();
            refreshOutputs = false;
        }

        BlockEntity beBehind = level.getBlockEntity(getBlockPos().relative(getFacing(getBlockState()).getOpposite()));
        if (beBehind instanceof SteelTankBlockEntity be) {
            SteelTankBlockEntity controllerBe = be.getControllerBE() == null ? be : be.getControllerBE();
            if (untilNextProcess > 0) {
                int toDecrement = controllerBe.activeHeat == 1 ? 1 : controllerBe.activeHeat / 2;
                untilNextProcess -= Math.max(0, toDecrement);
            } else {
                untilNextProcess = TFMGConfigs.common().machines.distillationRecipeGapTicks.get();
                manageRecipe(controllerBe);
            }
        }

        manageDialRendering();
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        refreshOutputs = true;
    }

    protected void onFluidStackChanged(FluidStack newFluidStack) {
        if (!hasLevel())
            return;

        if (!level.isClientSide) {
            setChanged();
            sendData();
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {

        BlockEntity beBehind = level.getBlockEntity(getBlockPos().relative(getFacing(getBlockState()).getOpposite()));
        if (beBehind instanceof SteelTankBlockEntity be) {
            SteelTankBlockEntity controllerBe = be.getControllerBE() == null ? be : be.getControllerBE();

            TFMGTexts.header("distillation_tower").style(ChatFormatting.GRAY).forGoggles(tooltip, 1);
            TFMGTexts.Distillation.level(controllerBe.activeHeat).forGoggles(tooltip, 1);
            TFMGTexts.Distillation.outputs(outputs.size()).forGoggles(tooltip, 1);
        } else
            TFMGTexts.Distillation.tankNotFound().forGoggles(tooltip, 1);

        TFMGUtils.createFluidTooltip(this,tooltip);

        return true;
    }

    public DistillationOutputBlockEntity getOutput(BlockPos pos) {
        if (level == null) return null;
        if (level.getBlockEntity(pos) instanceof DistillationOutputBlockEntity be) {
            return be;
        } else {
            refreshOutputs = true;
        }
        return null;
    }

    public void asOutput(BlockPos pos, Consumer<DistillationOutputBlockEntity> consumer) {
        if (level == null) return;
        if (level.getBlockEntity(pos) instanceof DistillationOutputBlockEntity be) {
            consumer.accept(be);
        } else {
            refreshOutputs = true;
        }
    }

    public void refreshOutputs() {
        ArrayList<BlockPos> outputs = new ArrayList<>();
        if (level == null) return;
        BlockPos checkedPos = this.getBlockPos().above();
        for (int i = 0; i < 11; i++) {
            if ((i % 2) == 0) {
                if (level.getBlockEntity(checkedPos) instanceof DistillationOutputBlockEntity) {
                    outputs.add(checkedPos);
                } else break;
            } else {
                if (!(level.getBlockState(checkedPos).is(TFMGTags.Blocks.INDUSTRIAL_PIPE.tag)))
                    break;
            }
            checkedPos = checkedPos.above();
        }
        this.outputs = outputs;
        this.sendData();
        this.setChanged();
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound,registries , clientPacket);
        tank.readFromNBT(registries,compound.getCompound("TankContent"));
        outputs = new ArrayList<>();
        for (int i = 0; i < compound.getInt("OutputCount"); i++) {
            NbtUtils.readBlockPos(compound, "Output" + i).ifPresent(output -> outputs.add(output));
        }
        this.untilNextProcess = compound.getInt("UntilNextProcess");
        updateCapability = true;
    }

    @Override
    public void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        compound.put("TankContent", tank.writeToNBT(registries,new CompoundTag()));
        compound.putInt("OutputCount", outputs.size());
        for (int i = 0; i < outputs.size(); i++) {
            BlockPos output = outputs.get(i);
            compound.put("Output" + i, NbtUtils.writeBlockPos(output));
        }
        compound.putInt("UntilNextProcess", this.untilNextProcess);
    }
}
