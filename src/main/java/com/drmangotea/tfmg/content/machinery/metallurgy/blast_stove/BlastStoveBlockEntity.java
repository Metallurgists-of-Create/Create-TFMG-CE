package com.drmangotea.tfmg.content.machinery.metallurgy.blast_stove;

import com.drmangotea.tfmg.base.fluid.ForceableFluidTank;
import com.drmangotea.tfmg.base.fluid.InputOutputTankWrapper;
import com.drmangotea.tfmg.base.lang.TFMGLang;
import com.drmangotea.tfmg.base.lang.TFMGTexts;
import com.drmangotea.tfmg.recipes.HotBlastRecipe;
import com.drmangotea.tfmg.registry.TFMGBlockEntities;
import com.drmangotea.tfmg.registry.TFMGRecipeTypes;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.recipe.RecipeConditions;
import com.simibubi.create.foundation.recipe.RecipeFinder;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.List;
import java.util.Objects;

import static net.neoforged.neoforge.fluids.FluidStack.isSameFluidSameComponents;

public class BlastStoveBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation, IMultiBlockEntityContainer.Fluid {
    private static final int MAX_SIZE = 2;
	
	protected IFluidHandler
		primaryCapability,
		secondaryCapability;
	protected ForceableFluidTank
		primaryOutputInventory,
		secondaryOutputInventory,
		primaryInputInventory,
		secondaryInputInventory;
    protected BlockPos controller;
    protected BlockPos lastKnownPos;
    public boolean updateConnectivity;
    private static final Object HotBlastRecipesKey = new Object();
    private static final int SYNC_RATE = 8;
	private HotBlastRecipe recipe;
    protected int syncCooldown;
    protected boolean queuedSync;
	protected int height = 1, width = 1;
    public int timer = 0;

    public boolean refreshCapability;

    public BlastStoveBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setLazyTickRate(10);
		int capacity = getCapacityMultiplier();
        primaryOutputInventory = new ForceableFluidTank(capacity, this::onFluidStackChanged).blockInsertion();
        secondaryOutputInventory = new ForceableFluidTank(capacity, this::onFluidStackChanged).blockInsertion();
        primaryInputInventory = new ForceableFluidTank(capacity, this::onFluidStackChanged).blockExtraction();
        secondaryInputInventory = new ForceableFluidTank(capacity, this::onFluidStackChanged).blockExtraction();
        primaryCapability = new InputOutputTankWrapper(primaryOutputInventory, secondaryInputInventory);
        secondaryCapability = new InputOutputTankWrapper(secondaryOutputInventory, primaryInputInventory);
		updateConnectivity = false;
		recipe = null;
        refreshCapability();
    }

    public void updateConnectivity() {
        updateConnectivity = false;
        if (!isController())
            return;

        for (int yOffset = 0; yOffset < height; yOffset++)
            for (int xOffset = 0; xOffset < width; xOffset++)
                for (int zOffset = 0; zOffset < width; zOffset++)
                    if (level.getBlockEntity(
                            worldPosition.offset(xOffset, yOffset, zOffset)) instanceof BlastStoveBlockEntity fbe)
                        fbe.refreshCapability();


        if (level.isClientSide)
            return;
        refreshCapability();

        ConnectivityHandler.formMulti(this);
		updateRecipe();
    }


    @Override
	public void tick() {
        super.tick();

        if (refreshCapability) {
            refreshCapability = false;
            refreshCapability();
        }

		if (level == null) return;
		if(!(level.isClientSide && !isVirtual()) &&
			isController() &&
			!primaryInputInventory.isEmpty() &&
			!secondaryInputInventory.isEmpty() &&
			primaryOutputInventory.getSpace() != 0 &&
			secondaryOutputInventory.getSpace() != 0
		) {
			if (recipe == null) updateRecipe();
			if (recipe != null) {
				if (timer >= getSpeed()) {
					if (
						(primaryOutputInventory.isEmpty() || isSameFluidSameComponents(primaryOutputInventory.getFluid(), recipe.getPrimaryResult())) &&
						(secondaryOutputInventory.isEmpty() || isSameFluidSameComponents(secondaryOutputInventory.getFluid(), recipe.getSecondaryResult()))  &&
						primaryOutputInventory.getSpace() >= recipe.getPrimaryResult().getAmount() &&
						secondaryOutputInventory.getSpace() >= recipe.getSecondaryResult().getAmount()
					) {
						primaryInputInventory.forceDrain(recipe.getPrimaryIngredient().amount(), IFluidHandler.FluidAction.EXECUTE);
						secondaryInputInventory.forceDrain(recipe.getSecondaryIngredient().amount(), IFluidHandler.FluidAction.EXECUTE);
						primaryOutputInventory.forceFill(recipe.getPrimaryResult(), IFluidHandler.FluidAction.EXECUTE);
						secondaryOutputInventory.forceFill(recipe.getSecondaryResult(), IFluidHandler.FluidAction.EXECUTE);
					}
					timer = 0;
				} else { timer++; }
			}
			refreshCapability = true;
        }

        if (syncCooldown > 0) {
            syncCooldown--;
            if (syncCooldown == 0 && queuedSync)
                sendData();
        }

        if (lastKnownPos == null)
            lastKnownPos = getBlockPos();
        else if (!lastKnownPos.equals(worldPosition)) {
            onPositionChanged();
            return;
        }
		
        if (updateConnectivity)
            updateConnectivity();
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
		updateRecipe();
		refreshCapability = true;
        updateConnectivity = true;
    }
	
	public int getTotalTankSize() {
		return width * width * height;
	}

    public int getSpeed () {
        return (int) (1000f / (getTotalTankSize() * 3));
    }

    protected Object getRecipeCacheKey() {
        return HotBlastRecipesKey;
    }

    protected void updateRecipe() {
        List<RecipeHolder<? extends Recipe<?>>> list = RecipeFinder.get(getRecipeCacheKey(), level, RecipeConditions.isOfType(TFMGRecipeTypes.HOT_BLAST.getType()));

        for (RecipeHolder<? extends Recipe<?>> recipeHolder : list) {
            HotBlastRecipe r = (HotBlastRecipe) recipeHolder.value();
            if (
				r.getPrimaryIngredient().test(primaryInputInventory.getFluid()) &&
				r.getSecondaryIngredient().test(secondaryInputInventory.getFluid())
			) {
				recipe = r;
                return;
			}
        }
		
		recipe = null;
    }

    @Override
    public BlockPos getLastKnownPos() {
        return lastKnownPos;
    }

    @Override
    public boolean isController() {
        return controller == null || worldPosition.getX() == controller.getX()
                && worldPosition.getY() == controller.getY() && worldPosition.getZ() == controller.getZ();
    }

    @Override
    public void initialize() {
        super.initialize();
        sendData();
        if (level.isClientSide)
            invalidateRenderBoundingBox();
    }

    private void onPositionChanged() {
        removeController(true);
        lastKnownPos = worldPosition;
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
    public void invalidate() {
        super.invalidate();
        invalidateCapabilities();
    }

    @SuppressWarnings("unchecked")
    @Override
    public BlastStoveBlockEntity getControllerBE() {
        if (isController())
            return this;
        if (level != null && level.getBlockEntity(controller) instanceof BlastStoveBlockEntity be)
            return be;
        return null;
    }

    public void applyFluidTankSize(int blocks) {

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

        onFluidStackChanged(primaryOutputInventory.getFluid());

        refreshCapability();
        setChanged();
        sendData();
    }

    public void sendDataImmediately() {
        syncCooldown = 0;
        queuedSync = false;
        sendData();
    }

    @Override
    public void sendData() {
        if (syncCooldown > 0) {
            queuedSync = true;
            return;
        }
        super.sendData();
        queuedSync = false;
        syncCooldown = SYNC_RATE;
    }


    @Override
    public void setController(BlockPos controller) {
        if (level.isClientSide && !isVirtual())
            return;
        if (controller.equals(this.controller))
            return;
        this.controller = controller;
        refreshCapability();
        setChanged();
        sendData();
    }

    public void refreshCapability() {
        primaryCapability = handlerForPrimaryCapability();
        secondaryCapability = handlerForSecondaryCapability();
		if (level == null) return;
        //TODO: invalidate caps correctly
		level.invalidateCapabilities(getBlockPos());
    }

    private IFluidHandler handlerForPrimaryCapability() {
		if (isController() || getControllerBE() == null)
			return new InputOutputTankWrapper(primaryOutputInventory, secondaryInputInventory);
		return getControllerBE().handlerForPrimaryCapability();
    }

    private IFluidHandler handlerForSecondaryCapability() {
		if (isController() || getControllerBE() == null)
			return new InputOutputTankWrapper(secondaryOutputInventory, primaryInputInventory);
        return getControllerBE().handlerForSecondaryCapability();
    }

    @Override
    public BlockPos getController() {
        return isController() ? worldPosition : controller;
    }

    @Override
    protected AABB createRenderBoundingBox() {
        if (isController())
            return super.createRenderBoundingBox().expandTowards(width - 1, height - 1, width - 1);
        else
            return super.createRenderBoundingBox();
    }


    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);

        BlockPos controllerBefore = controller;
        int prevSize = width;
        int prevHeight = height;

        updateConnectivity = compound.contains("Uninitialized");
        controller = null;
        lastKnownPos = null;

        if (compound.contains("LastKnownPos"))
            lastKnownPos = NbtUtils.readBlockPos(compound, "LastKnownPos").get();
        if (compound.contains("Controller"))
            controller = NbtUtils.readBlockPos(compound, "Controller").get();

        if (isController()) {
            width = compound.getInt("Size");
            height = compound.getInt("Height");
            primaryOutputInventory.readFromNBT(registries, compound.getCompound("primaryOutputInventory"));
            primaryInputInventory.readFromNBT(registries, compound.getCompound("primaryInputInventory"));
            secondaryOutputInventory.readFromNBT(registries, compound.getCompound("secondaryOutputInventory"));
            secondaryInputInventory.readFromNBT(registries, compound.getCompound("secondaryInputInventory"));
            if (primaryOutputInventory.getSpace() < 0)
                primaryOutputInventory.drain(-primaryOutputInventory.getSpace(), IFluidHandler.FluidAction.EXECUTE);
        }

        timer = compound.getInt("Timer");

        refreshCapability = true;

        if (!clientPacket)
            return;

        boolean changeOfController = !Objects.equals(controllerBefore, controller);
        if (changeOfController || prevSize != width || prevHeight != height) {
            if (level != null)
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 16);
            invalidateRenderBoundingBox();
        }
    }

    @Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (getControllerBE() == null) { return false; }
		
		IFluidHandler pri = getControllerBE().primaryCapability;
		IFluidHandler sec = getControllerBE().secondaryCapability;

        TFMGTexts.header("blast_stove").forGoggles(tooltip);
        tankTooltip(tooltip, "goggles.blast_stove.tank1", sec.getFluidInTank(1), ChatFormatting.DARK_GREEN);
        tankTooltip(tooltip, "goggles.blast_stove.tank2", pri.getFluidInTank(1), ChatFormatting.DARK_GREEN);
        tankTooltip(tooltip, "goggles.blast_stove.tank3", pri.getFluidInTank(0), ChatFormatting.YELLOW);
        tankTooltip(tooltip, "goggles.blast_stove.tank4", sec.getFluidInTank(0), ChatFormatting.YELLOW);
        return true;
    }
	
	private void tankTooltip (List<Component> tooltip, String key, FluidStack fluid, ChatFormatting color) {
		LangBuilder mb = CreateLang.translate("generic.unit.millibuckets");
		LangBuilder name = fluid.getFluid() == Fluids.EMPTY ? TFMGLang.text("") :  TFMGLang.text(" "+fluid.getHoverName().getString());
	
		TFMGLang.builder()
			.add(TFMGLang.translate(key))
			.add(TFMGLang.number(fluid.getAmount()).add(mb).add(name).style(color))
			.text(ChatFormatting.GRAY, " / ")
			.add(TFMGLang.number(getCapacityMultiplier()).add(mb).style(ChatFormatting.DARK_GRAY))
			.forGoggles(tooltip, 1);
	}


    @Override
    public void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {

        if (updateConnectivity)
            compound.putBoolean("Uninitialized", true);

        if (lastKnownPos != null)
            compound.put("LastKnownPos", NbtUtils.writeBlockPos(lastKnownPos));
        if (!isController())
            compound.put("Controller", NbtUtils.writeBlockPos(controller));
        if (isController()) {
            compound.put("primaryOutputInventory", primaryOutputInventory.writeToNBT(registries, new CompoundTag()));
            compound.put("primaryInputInventory", primaryInputInventory.writeToNBT(registries, new CompoundTag()));
            compound.put("secondaryOutputInventory", secondaryOutputInventory.writeToNBT(registries, new CompoundTag()));
            compound.put("secondaryInputInventory", secondaryInputInventory.writeToNBT(registries, new CompoundTag()));
            compound.putInt("Size", width);
            compound.putInt("Height", height);
        }

        compound.putInt("Timer", timer);

        forEachBehaviour(tb -> tb.write(compound, registries, clientPacket));

        if (!clientPacket)
            return;
        if (queuedSync)
            compound.putBoolean("LazySync", true);

    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, TFMGBlockEntities.BLAST_STOVE.get(),
			(be, dir) -> {
                BlastStoveBlockEntity controller = be.getControllerBE();
                if (controller == null)
                    return null;

                if (controller.primaryCapability == null || controller.secondaryCapability == null)
                    controller.refreshCapability();

                if (dir == null || dir.getAxis().isVertical())
                    return controller.primaryCapability;
                if (be.getController().getY() == be.getBlockPos().getY())
                    return controller.secondaryCapability;
				
				return null;
			}
        );
    }
	
	@Override
	public int getHeight() { return height; }
	
	@Override
	public void setHeight(int height) { this.height = height; }
	
	@Override
	public int getWidth() { return width; }
	
	@Override
	public void setWidth(int width) { this.width = width; }
	
	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) { }

    public FluidTank getTank () {
        return primaryOutputInventory;
    }
	
	public FluidStack getFluid () {
		return primaryOutputInventory.getFluid().copy();
	}
	
	public static int getCapacityMultiplier() {
		return AllConfigs.server().fluids.fluidTankCapacity.get() * 1000;
	}

    public static int getMaxHeight() {
        return AllConfigs.server().fluids.fluidTankMaxHeight.get();
    }

    @Override
    public void preventConnectivityUpdate() {
        updateConnectivity = false;
    }

    @Override
    public void notifyMultiUpdated() {
        onFluidStackChanged(primaryOutputInventory.getFluid());
        setChanged();
        updateConnectivity = true;

        sendData();
        setChanged();
    }

    @Override
    public Direction.Axis getMainConnectionAxis() {
        return Direction.Axis.Y;
    }

    @Override
    public int getMaxLength(Direction.Axis longAxis, int width) {
        if (longAxis == Direction.Axis.Y)
            return getMaxHeight();
        return getMaxWidth();
    }
	
	@Override
	public int getMaxWidth() { return MAX_SIZE; }
}