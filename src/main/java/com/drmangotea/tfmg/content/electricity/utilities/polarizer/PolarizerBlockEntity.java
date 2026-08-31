package com.drmangotea.tfmg.content.electricity.utilities.polarizer;

import com.drmangotea.tfmg.base.lang.TFMGTexts;
import com.drmangotea.tfmg.config.TFMGConfigs;
import com.drmangotea.tfmg.content.electricity.base.ElectricBlockEntity;
import com.drmangotea.tfmg.content.electricity.measurement.MultimeterItem;
import com.drmangotea.tfmg.recipes.PolarizingRecipe;
import com.drmangotea.tfmg.registry.TFMGBlockEntities;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.item.SmartInventory;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Clearable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;

public class PolarizerBlockEntity extends ElectricBlockEntity implements IHaveGoggleInformation, Clearable {
    public SmartInventory inventory = new LetMeKeepMyItemSmartInventory(1, this).forbidExtraction().whenContentsChanged(this::onInventoryChanged);
    public SmartInventory outputInventory = new SmartInventory(1, this, 1, false).forbidInsertion().whenContentsChanged(this::onInventoryChanged);

    public IItemHandlerModifiable itemCapability;
    public PolarizingRecipe recipe;
    LerpedFloat angle = LerpedFloat.angular();

    public boolean chargeCapacitors = false;
    public int capacitorPercentage = 0;

    public PolarizerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        itemCapability = new CombinedInvWrapper(inventory, outputInventory);
    }

    public ItemStack getInputItem() {
        return inventory.getStackInSlot(0);
    }

    public ItemStack getOutputItem() {
        return outputInventory.getStackInSlot(0);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                TFMGBlockEntities.POLARIZER.get(),
                (be, context) -> be.itemCapability
        );
    }

    @Override
    public boolean hasElectricitySlot(Direction direction) {
        return direction == getBlockState().getValue(FACING).getOpposite();
    }

    public void onInventoryChanged(int count) {
        if (level == null) return;
        sendData();
        setChanged();
        level.invalidateCapabilities(this.getPos());
        if (inventory.isEmpty()) {
            chargeCapacitors = false;
            recipe = null;
            capacitorPercentage = 0;
            updateNextTick();
            return;
        }
        ItemStack itemStack = inventory.getItem(0);
        Optional<PolarizingRecipe> recipe = PolarizerCommons.getRecipe(this.level, itemStack).map(RecipeHolder::value);
        if (recipe.isPresent()) {
            this.recipe = recipe.get();
            chargeCapacitors = true;
            updateNextTick();
            if (capacitorPercentage >= 200) {
                performRecipe(recipe.get());
            }
        } else {
            chargeCapacitors = false;
            this.recipe = null;
            updateNextTick();
        }
    }

    @Override
    public float resistance() {
        return chargeCapacitors ? 30 : 0;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        TFMGTexts.header("polarizer").style(ChatFormatting.GRAY).forGoggles(tooltip, 1);
        TFMGTexts.Multimeter.charge(Math.round(capacitorPercentage / 2f)).forGoggles(tooltip);
        if (recipe != null && !inventory.isEmpty() && getPowerUsage() < recipe.energy) {
            TFMGTexts.Multimeter.notEnoughPower(recipe.energy).forGoggles(tooltip, 1);
            return true;
        }
        if (recipe != null && capacitorPercentage >= 200 && !canFitOutput(recipe)) {
            TFMGTexts.header("outputFull").style(ChatFormatting.RED).forGoggles(tooltip, 1);
            return true;
        }
        if (Minecraft.getInstance().player != null
                && MultimeterItem.isHeldByPlayer(Minecraft.getInstance().player))
            makeMultimeterTooltip(tooltip, isPlayerSneaking);
        return true;
    }

    @Override
    public void tick() {
        if (level == null) return;
        super.tick();

        if (level.isClientSide) {
            angle.chase(180 * (capacitorPercentage / 200f), 0.2f, LerpedFloat.Chaser.EXP);
            angle.tickChaser();
            return;
        }

        if (chargeCapacitors && recipe != null && getPowerUsage() >= recipe.energy && capacitorPercentage < 200) {
            capacitorPercentage++;
            sendData();
        }

        if (chargeCapacitors && recipe != null && capacitorPercentage >= 200) {
            performRecipe(recipe);
        }
    }

    private boolean canFitOutput(PolarizingRecipe recipe) {
        if (level == null) return false;
        ItemStack simulatedResult = PolarizerCommons.assembleResult(level, getBlockPos().getCenter(), recipe);
        ItemStack current = outputInventory.getStackInSlot(0);
        if (current.isEmpty()) return true;
        if (!ItemStack.isSameItemSameComponents(current, simulatedResult)) return false;
        return current.getCount() + simulatedResult.getCount() > current.getMaxStackSize();
    }

    public void performRecipe(PolarizingRecipe recipe) {
        if (level == null || !canFitOutput(recipe)) return;

        ItemStack result = PolarizerCommons.assembleResult(level, getBlockPos().getCenter(), recipe);

        inventory.setStackInSlot(0, ItemStack.EMPTY);
        this.recipe = null;
        capacitorPercentage = 0;

        ItemStack current = outputInventory.getStackInSlot(0);
        if (current.isEmpty()) {
            outputInventory.setStackInSlot(0, result);
        } else {
            ItemStack merged = current.copy();
            merged.grow(result.getCount());
            outputInventory.setStackInSlot(0, merged);
        }

        sendData();
        setChanged();
    }

    // Is this really needed?
    @Deprecated(forRemoval = true, since = "1.2.5-community")
    public int getItemChargingRate() {
        return TFMGConfigs.common().machines.polarizerItemChargingRate.get();
    }

    @Override
    public void destroy() {
        if (level == null || level.isClientSide) return;
        ItemHelper.dropContents(level, getBlockPos(), inventory);
        ItemHelper.dropContents(level, getBlockPos(), outputInventory);
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        compound.put("Inventory", inventory.serializeNBT(registries));
        compound.put("OutputInventory", outputInventory.serializeNBT(registries));
        compound.putInt("CapacitorPercentage", capacitorPercentage);
        compound.putBoolean("ChargeCapacitors", chargeCapacitors);
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        inventory.deserializeNBT(registries, compound.getCompound("Inventory"));
        // Wrap in if it contains to not brick loading on older previous versions
        if (compound.contains("OutputInventory")) {
            outputInventory.deserializeNBT(registries, compound.getCompound("OutputInventory"));
        }
        capacitorPercentage = compound.getInt("CapacitorPercentage");
        chargeCapacitors = compound.getBoolean("ChargeCapacitors");
    }

    @Override
    public void clearContent() {
        this.inventory.clearContent();
        this.outputInventory.clearContent();
    }

    private static class LetMeKeepMyItemSmartInventory extends SmartInventory {
        private final PolarizerBlockEntity be;

        public LetMeKeepMyItemSmartInventory(int slots, PolarizerBlockEntity be) {
            super(slots, be, 1, false);
            this.be = be;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return be.outputInventory.isEmpty() ? super.insertItem(slot, stack, simulate) : stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (be.capacitorPercentage > 0 && be.capacitorPercentage < 200) {
                return ItemStack.EMPTY;
            }
            return super.extractItem(slot, amount, simulate);
        }
    }
}