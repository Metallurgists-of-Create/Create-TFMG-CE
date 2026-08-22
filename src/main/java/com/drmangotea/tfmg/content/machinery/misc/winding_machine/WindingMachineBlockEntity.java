package com.drmangotea.tfmg.content.machinery.misc.winding_machine;

import com.drmangotea.tfmg.base.lang.TFMGLang;
import com.drmangotea.tfmg.base.lang.TFMGTexts;
import com.drmangotea.tfmg.recipes.WindingRecipe;
import com.drmangotea.tfmg.registry.*;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.item.SmartInventory;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

import java.util.List;
import java.util.Optional;

import static com.drmangotea.tfmg.content.machinery.misc.winding_machine.WindingMachineBlock.POWERED;
import static com.simibubi.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING;

public class WindingMachineBlockEntity extends KineticBlockEntity implements IHaveGoggleInformation {
    LerpedFloat spoolSpeed = LerpedFloat.linear();
    float angle;
    public SmartInventory inventory;
    public SmartInventory outputInventory;
    public SmartInventory spoolInventory;
    public WindingRecipe recipe;
    public int amountWinded = 0;
    public boolean update = false;

    protected ScrollValueBehaviour turnPercentage;

    public WindingMachineBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        setLazyTickRate(10);
        inventory = new SmartInventory(1, this)
                .withMaxStackSize(1)
                .allowInsertion()
                .forbidExtraction()
                .whenContentsChanged(i -> this.onContentsChanged());

        outputInventory = new SmartInventory(1, this)
                .withMaxStackSize(1)
                .allowExtraction()
                .forbidInsertion()
                .whenContentsChanged(i -> this.onContentsChanged());

        spoolInventory = new SmartInventory(1, this, (slot, stack) -> stack.getItem() instanceof SpoolItem)
                .withMaxStackSize(1)
                .whenContentsChanged(i -> this.onContentsChanged());
    }


    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                TFMGBlockEntities.WINDING_MACHINE.get(),
                (be, context) -> {
                    if (context == be.getBlockState().getValue(HORIZONTAL_FACING))
                        return new CombinedInvWrapper(new InputSlotHandler(be), be.outputInventory, new SpoolSlotHandler(be));
                    return new CombinedInvWrapper(new InputSlotHandler(be), be.outputInventory);
                }
        );
    }


    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        int max = 100;
        turnPercentage = new ScrollValueBehaviour(TFMGLang.translateDirect("winding_machine.turn_percentage"),
                this, new WindingMachineValueBox());
        turnPercentage.between(1, max);
        turnPercentage.value = 20;
        behaviours.add(turnPercentage);
    }

    public void onContentsChanged() {
        findRecipe();
        if (inventory.isEmpty())
            amountWinded = 0;
    }

    public ItemStack getSpool() {
        return spoolInventory.getItem(0);
    }

    public void setSpool(ItemStack stack) {
        spoolInventory.setStackInSlot(0, stack);
    }

    public boolean isWindingIngredient(ItemStack stack) {
        return !stack.isEmpty() && !(stack.getItem() instanceof SpoolItem);
    }

    public void findRecipe() {
        if (level == null) {
            return;
        }
        Optional<RecipeHolder<WindingRecipe>> optional = TFMGRecipeTypes.WINDING.find(new RecipeWrapper(inventory), level);
        Optional<RecipeHolder<WindingRecipe>> assemblyRecipe = SequencedAssemblyRecipe.getRecipe(this.level, new RecipeWrapper(inventory), TFMGRecipeTypes.WINDING.getType(), WindingRecipe.class);

        if (assemblyRecipe.isPresent()) {
            recipe = assemblyRecipe.get().value();
            return;
        }
        if (optional.isEmpty()) {
            recipe = null;
            return;
        }
        WindingRecipe windingRecipe = optional.get().value();

        if (windingRecipe.getIngredient().test(inventory.getItem(0)) && outputInventory.isEmpty()) {
            recipe = windingRecipe;
        }
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        onContentsChanged();
        if (level == null) {
            return;
        }
        if (getSpool().is(TFMGItems.EMPTY_SPOOL.get()) && !getBlockState().getValue(POWERED)) {
            level.setBlock(getBlockPos(), getBlockState().setValue(POWERED, true), 2);
            update = true;
        }
        if (!getSpool().is(TFMGItems.EMPTY_SPOOL.get()) && getBlockState().getValue(POWERED)) {
            level.setBlock(getBlockPos(), getBlockState().setValue(POWERED, false), 2);
            update = true;
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        TFMGTexts.header("winding_machine")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 1);

        if (!getSpool().isEmpty()) {
            TFMGLang.text(getSpool().getDisplayName().getString().replace("[","").replace("]",""))
                    .color(getSpool().getBarColor())
                    .forGoggles(tooltip);
            if(getSpool().get(TFMGDataComponents.SPOOL_AMOUNT)!=null)
                TFMGTexts.turnsLeft(getSpool().getOrDefault(TFMGDataComponents.SPOOL_AMOUNT, 0))
                    .color(getSpool().getBarColor())
                    .forGoggles(tooltip);

        if (recipe != null)
            TFMGTexts.progress(amountWinded + "/" + recipe.getProcessingDuration())
                    .color(getSpool().getBarColor())
                    .forGoggles(tooltip);
        }
        return true;
    }

    public void destroy() {
        super.destroy();
        if (level == null || level.isClientSide) {
            return;
        }
        ItemHelper.dropContents(level, worldPosition, inventory);
        ItemHelper.dropContents(level, worldPosition, outputInventory);
        ItemHelper.dropContents(level, worldPosition, spoolInventory);
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null) {
            return;
        }
        performRecipe();
        if (update) {
            level.updateNeighborsAt(getBlockPos(), getBlockState().getBlock());
            update = false;
        }

        manageRotation();
    }

    public void performRecipe() {
        //Change these if you want. Just fallbacks if the component is null.
        int defaultResistance = 0;
        int defaultSpoolAmount = 0;
        int defaultCoilTurns = 0;
        if (level == null) {
            return;
        }

        if (getSpeed() == 0 || !outputInventory.isEmpty())
            return;

        //TODO: change whatever these two if statements are
        if ((inventory.getItem(0).is(TFMGItems.ELECTROMAGNETIC_COIL.get()) || inventory.getItem(0).is(TFMGBlocks.LARGE_COIL.get().asItem())) && getSpool().is(TFMGItems.COPPER_SPOOL.get()) && getSpool().getOrDefault(TFMGDataComponents.SPOOL_AMOUNT, defaultSpoolAmount) > 0 && inventory.getItem(0).getOrDefault(TFMGDataComponents.COIL_TURNS, defaultCoilTurns) < turnPercentage.getValue() * 10) {
            if(inventory.getItem(0).getOrDefault(TFMGDataComponents.COIL_TURNS, defaultCoilTurns) < turnPercentage.getValue() * 10){
                getSpool().set(TFMGDataComponents.SPOOL_AMOUNT, getSpool().getOrDefault(TFMGDataComponents.SPOOL_AMOUNT, defaultSpoolAmount) - 1);
                inventory.getItem(0).set(TFMGDataComponents.COIL_TURNS, inventory.getItem(0).getOrDefault(TFMGDataComponents.COIL_TURNS, defaultCoilTurns) + 1);
                return;
            }
        }
        if(getSpool().has(TFMGDataComponents.SPOOL_AMOUNT))
            if (inventory.getItem(0).is(TFMGBlocks.RESISTOR.asItem()) && getSpool().is(TFMGItems.CONSTANTAN_SPOOL.get()) && getSpool().getOrDefault(TFMGDataComponents.SPOOL_AMOUNT, defaultSpoolAmount) > 0 && inventory.getItem(0).getOrDefault(TFMGDataComponents.RESISTANCE, defaultResistance) < turnPercentage.getValue() * 10) {
                if(inventory.getItem(0).getOrDefault(TFMGDataComponents.RESISTANCE, 0)< turnPercentage.getValue() * 10) {
                    getSpool().set(TFMGDataComponents.SPOOL_AMOUNT, getSpool().getOrDefault(TFMGDataComponents.SPOOL_AMOUNT, defaultSpoolAmount) - 1);
                    inventory.getItem(0).set(TFMGDataComponents.RESISTANCE, inventory.getItem(0).getOrDefault(TFMGDataComponents.RESISTANCE, defaultResistance) + 1);
                    return;
                }
            }

        if(getSpool().has(TFMGDataComponents.SPOOL_AMOUNT))
            if (getSpool().getOrDefault(TFMGDataComponents.SPOOL_AMOUNT, defaultSpoolAmount) == 0 && !getSpool().is(TFMGItems.EMPTY_SPOOL.get()) && getSpool().getItem() instanceof SpoolItem) {
                setSpool(TFMGItems.EMPTY_SPOOL.asStack());
                sendData();
                setChanged();
                depositEmptySpool();
            }

        if (recipe == null) {
            return;
        }

        if (amountWinded >= recipe.getProcessingDuration()) {
            ItemStack result = recipe.rollResults(level.random).getFirst();

            inventory.setStackInSlot(0, ItemStack.EMPTY);
            outputInventory.setStackInSlot(0, result);
            recipe = null;
            amountWinded = 0;

            sendData();
            setChanged();
        } else {
            if (getSpool().isEmpty() || getSpool().is(TFMGItems.EMPTY_SPOOL.get())) {
                return;
            }
            if (getSpool().getOrDefault(TFMGDataComponents.SPOOL_AMOUNT, defaultCoilTurns) > 0) {
                if (recipe.getSpool().test(getSpool())) {
                    getSpool().set(TFMGDataComponents.SPOOL_AMOUNT, getSpool().getOrDefault(TFMGDataComponents.SPOOL_AMOUNT, defaultCoilTurns) - 1);
                    amountWinded++;
                }
            } else {
                ItemStack result = recipe.rollResults(level.random).getFirst();

                inventory.setStackInSlot(0, ItemStack.EMPTY);
                outputInventory.setStackInSlot(0, result);
                recipe = null;
                amountWinded = 0;
                sendData();
                setChanged();
            }

        }
    }

    public void manageRotation() {
        float targetSpeed = (float) Math.min(Math.abs(getSpeed() * 1.5), 30);
        spoolSpeed.updateChaseTarget(targetSpeed);
        spoolSpeed.tickChaser();
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound,registries , clientPacket);
        compound.put("Inventory", inventory.serializeNBT(registries));
        compound.put("OutputInventory", outputInventory.serializeNBT(registries));
        compound.put("SpoolInventory", spoolInventory.serializeNBT(registries));
        compound.putInt("AmountWinded", amountWinded);
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound,registries , clientPacket);
        inventory.deserializeNBT(registries, compound.getCompound("Inventory"));
        outputInventory.deserializeNBT(registries, compound.getCompound("OutputInventory"));
        // legacy support and migrate to new system
        if (compound.contains("SpoolInventory")) {
            spoolInventory.deserializeNBT(registries, compound.getCompound("SpoolInventory"));
        } else if (compound.contains("Spool")) {
            spoolInventory.setStackInSlot(0, ItemStack.parseOptional(registries, compound.getCompound("Spool")));
            compound.remove("Spool");
        }
        amountWinded = compound.getInt("AmountWinded");
        if (clientPacket)
            spoolSpeed.chase(getGeneratedSpeed(), 1 / 16f, LerpedFloat.Chaser.EXP);
    }

    public void depositEmptySpool() {
        if (level == null || level.isClientSide || getSpool().isEmpty())
            return;
        Direction facing = getBlockState().getValue(HORIZONTAL_FACING);
        IItemHandler handler = level.getCapability(
                Capabilities.ItemHandler.BLOCK, worldPosition.relative(facing), facing.getOpposite());
        if (handler == null)
            return;
        for (int i = 0; i < handler.getSlots(); i++) {
            if (handler.getStackInSlot(i).isEmpty()) {
                handler.insertItem(i, getSpool(), false);
                spoolInventory.setStackInSlot(0, ItemStack.EMPTY);
                sendData();
                setChanged();
                return;
            }
        }
    }

    private record InputSlotHandler(WindingMachineBlockEntity be) implements IItemHandlerModifiable {
        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            be.inventory.setStackInSlot(slot, stack);
            be.onContentsChanged();
            be.notifyUpdate();
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return be.inventory.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.isEmpty())
                return stack;

            if (!be.outputInventory.isEmpty())
                return stack;

            if (!be.inventory.getStackInSlot(slot).isEmpty())
                return stack;

            if (!isItemValid(slot, stack))
                return stack;

            if (!simulate) {
                be.inventory.setStackInSlot(slot, stack.copy());
                be.onContentsChanged();
                be.notifyUpdate();
            }
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return be.isWindingIngredient(stack);
        }
    }

    @MethodsReturnNonnullByDefault
    private record SpoolSlotHandler(WindingMachineBlockEntity be) implements IItemHandlerModifiable {

        @Override
            public int getSlots() {
                return 1;
            }

            @Override
            public ItemStack getStackInSlot(int slot) {
                return be.spoolInventory.getStackInSlot(slot);
            }

            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                if (stack.isEmpty() || !(stack.getItem() instanceof SpoolItem))
                    return stack;
                if (!be.getSpool().isEmpty())
                    return stack;
                if (!simulate) {
                    be.spoolInventory.setStackInSlot(slot, stack.copy());
                    be.onContentsChanged();
                    be.notifyUpdate();
                }
                return ItemStack.EMPTY;
            }

            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                ItemStack stackInSlot = be.spoolInventory.getStackInSlot(slot);
                if (stackInSlot.isEmpty() || !stackInSlot.is(TFMGItems.EMPTY_SPOOL.get()))
                    return ItemStack.EMPTY;
                int extracted = Math.min(amount, stackInSlot.getCount());
                if (!simulate) {
                    be.spoolInventory.setStackInSlot(slot, ItemStack.EMPTY);
                    be.onContentsChanged();
                    be.notifyUpdate();
                }
                return new ItemStack(stackInSlot.getItem(), extracted);
            }

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return stack.getItem() instanceof SpoolItem;
            }

            @Override
            public void setStackInSlot(int slot, ItemStack stack) {
                be.spoolInventory.setStackInSlot(slot, stack);
                be.onContentsChanged();
                be.notifyUpdate();
            }
        }

    public static class WindingMachineValueBox extends ValueBoxTransform.Sided {
        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 4, 16.05);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return direction == state.getValue(HORIZONTAL_FACING);
        }
    }
}
