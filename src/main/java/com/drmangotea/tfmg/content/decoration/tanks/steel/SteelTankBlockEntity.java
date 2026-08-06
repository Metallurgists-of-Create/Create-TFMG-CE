package com.drmangotea.tfmg.content.decoration.tanks.steel;

import com.drmangotea.tfmg.content.decoration.tanks.TFMGFluidTankBlockEntity;
import com.drmangotea.tfmg.mixin.accessor.FluidTankBlockEntityAccessor;
import com.drmangotea.tfmg.registry.TFMGBlockEntities;
import com.drmangotea.tfmg.registry.TFMGBlocks;
import com.simibubi.create.api.boiler.BoilerHeater;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.fluids.tank.FluidTankBlock;
import com.simibubi.create.content.fluids.tank.FluidTankBlock.Shape;
import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.List;

public class SteelTankBlockEntity extends TFMGFluidTankBlockEntity implements IHaveGoggleInformation, IMultiBlockEntityContainer.Fluid {
    public int gaugeRotation = 0;
    public int activeHeat;
    public boolean isDistillationTower = false;

    // For rendering purposes only
    private LerpedFloat fluidLevel;
    public LerpedFloat visualGaugeRotation = LerpedFloat.angular();
    public SteelTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setLazyTickRate(10);
        tankInventory = createInventory();
        fluidCapability = tankInventory;
        forceFluidLevelUpdate = true;
        updateConnectivity = false;
        window = true;
        height = 1;
        width = 1;
        ((FluidTankBlockEntityAccessor)this).tfmg$refreshCapability();
    }

    protected SmartFluidTank createInventory() {
        return new SmartFluidTank(getCapacityMultiplier(), this::onFluidStackChanged);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                TFMGBlockEntities.STEEL_FLUID_TANK.get(),
                (be, context) -> {
                    if (be.fluidCapability == null)
                        ((FluidTankBlockEntityAccessor)be).tfmg$refreshCapability();
                    return be.fluidCapability;
                }
        );
    }

    @Override
    public void tick() {
        getGaugeRotation();
        visualGaugeRotation.chase(gaugeRotation, 0.2f, LerpedFloat.Chaser.EXP);
        visualGaugeRotation.tickChaser();
		super.tick();
    }

    @Override
    public void initialize() {
        super.initialize();
        sendData();
        if (level.isClientSide)
            invalidateRenderBoundingBox();
    }

    protected void onFluidStackChanged(FluidStack newFluidStack) {
        if (!hasLevel())
            return;
        FluidType attributes = newFluidStack.getFluid()
                .getFluidType();
        int luminosity = (int) (attributes.getLightLevel(newFluidStack) / 1.2f);
        boolean reversed = attributes.isLighterThanAir();
        int maxY = (int) ((getFillState() * height) + 1);
        for (int yOffset = 0; yOffset < height; yOffset++) {
            boolean isBright = reversed ? (height - yOffset <= maxY) : (yOffset < maxY);
            int actualLuminosity = isBright ? luminosity : luminosity > 0 ? 1 : 0;
            for (int xOffset = 0; xOffset < width; xOffset++) {
                for (int zOffset = 0; zOffset < width; zOffset++) {
                    BlockPos pos = this.worldPosition.offset(xOffset, yOffset, zOffset);
                    SteelTankBlockEntity tankAt = ConnectivityHandler.partAt(getType(), level, pos);
                    if (tankAt == null)
                        continue;
                    level.updateNeighbourForOutputSignal(pos, tankAt.getBlockState()
                            .getBlock());
                    if (tankAt.luminosity == actualLuminosity)
                        continue;
                    tankAt.setLuminosity(actualLuminosity);
                }
            }
        }

        if (!level.isClientSide) {
            setChanged();
            sendData();
        }

        if (isVirtual()) {
            if (fluidLevel == null)
                fluidLevel = LerpedFloat.linear().startWithValue(getFillState());
            fluidLevel.chase(getFillState(), .5f, LerpedFloat.Chaser.EXP);
        }
    }
	
	@Override
	public SteelTankBlockEntity getControllerBE() {
		if (isController()) return this;
		if (level != null && level.getBlockEntity(controller) instanceof SteelTankBlockEntity be)
			return be;
		return null;
	}

    public void removeController(boolean keepFluids) {
        if (level.isClientSide)
            return;
        updateConnectivity = true;
        if (!keepFluids)
            applyFluidTankSize(1);
        controller = null;
        width = 1;
        height = 1;

        onFluidStackChanged(tankInventory.getFluid());

        BlockState state = getBlockState();
        if (isTank(state)) {
            state = state
				.setValue(FluidTankBlock.BOTTOM, true)
				.setValue(FluidTankBlock.TOP, true)
				.setValue(FluidTankBlock.SHAPE, window ? Shape.WINDOW : Shape.PLAIN);
            getLevel().setBlock(worldPosition, state, 22);
        }
        ((FluidTankBlockEntityAccessor)this).tfmg$refreshCapability();
        setChanged();
        sendData();
    }

    public void toggleWindows() {
        SteelTankBlockEntity be = getControllerBE();
        if (be == null || be.isDistillationTower) return;
        be.setWindows(!be.window);
    }

    public void updateBoilerState() {
        if (!isController() || getControllerBE() == null) return;

        boolean changed = evaluate();

        if (changed) {
            if (isDistillationTower) setWindows(false);
            for (int Y = 0; Y < height; Y++) { for (int X = 0; X < width; X++) { for (int Z = 0; Z < width; Z++) {
				if (level.getBlockEntity(worldPosition.offset(X, Y, Z)) instanceof SteelTankBlockEntity fte)
					((FluidTankBlockEntityAccessor) fte).tfmg$refreshCapability();
			}}}
			
            notifyUpdate();
            ((FluidTankBlockEntityAccessor)this).tfmg$refreshCapability();
        }
    }
	
    public boolean evaluate() {
        boolean hadController = isDistillationTower;
        boolean foundController = false;
        BlockPos pos1 = controller == null ? getBlockPos() : controller;
        for (int Y = 0; Y < getControllerBE().height; Y++) { for (int X = 0; X < getControllerBE().width; X++) { for (int Z = 0; Z < getControllerBE().width; Z++) {
			BlockPos pos = pos1.offset(X, Y, Z);
			BlockState blockState = level.getBlockState(pos);
			if (!isTank(blockState)) continue;
			for (Direction d : Iterate.directions) {
				BlockPos attachedPos = pos.relative(d);
				BlockState attachedState = level.getBlockState(attachedPos);

				if (attachedState.is(TFMGBlocks.STEEL_DISTILLATION_CONTROLLER.get())) {
					if (!foundController) { foundController = true; }
					else level.destroyBlock(attachedPos, true);
				}
			}
		}}}
        isDistillationTower = foundController;
        return hadController != foundController;
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (isDistillationTower)
            updateTemperature();
    }
	
    public void updateTemperature() {
        int prevHeat = activeHeat;
        activeHeat = 0;
        BlockPos pos1 = controller == null ? getBlockPos() : controller;
        SteelTankBlockEntity be = getControllerBE() == null ? this : getControllerBE();

        for (int xOffset = 0; xOffset < be.width; xOffset++) {
            for (int zOffset = 0; zOffset < be.width; zOffset++) {
                BlockPos pos = pos1.offset(xOffset, -1, zOffset);
                BlockState blockState = level.getBlockState(pos);
                float heat = BoilerHeater.findHeat(level, pos, blockState);
                if (heat > 0) {
                    activeHeat += heat;
                }
            }
        }

        if (activeHeat != prevHeat)
            notifyUpdate();
    }

    @Override
    protected AABB createRenderBoundingBox() {
        if (isController())
            return super.createRenderBoundingBox().expandTowards(width - 1, height - 1, width - 1);
        else
            return super.createRenderBoundingBox();
    }
	
    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        SteelTankBlockEntity controllerBE = getControllerBE();
        if (isDistillationTower || controllerBE == null || controllerBE.isDistillationTower)
            return false;

        return containedFluidTooltip(tooltip, isPlayerSneaking,
                level.getCapability(Capabilities.FluidHandler.BLOCK, controllerBE.getBlockPos(), null));
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        if (isController()) { isDistillationTower = compound.getBoolean("IsDistillationTower"); }
    }
	
    public void getGaugeRotation() {
        gaugeRotation = Math.min(90, activeHeat * 15);
    }
	
	@Override
    public void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        if (isController()) { compound.putBoolean("IsDistillationTower",isDistillationTower); }
        super.write(compound, registries, clientPacket);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        //registerAwardables(behaviours, AllAdvancements.STEAM_ENGINE_MAXED, AllAdvancements.PIPE_ORGAN);
    }
	
    public LerpedFloat getFluidLevel() {
        return fluidLevel;
    }
    public void setFluidLevel(LerpedFloat fluidLevel) {
        this.fluidLevel = fluidLevel;
    }
	
	public boolean isTank(BlockState state) {return SteelTankBlock.isTank(state);}
}