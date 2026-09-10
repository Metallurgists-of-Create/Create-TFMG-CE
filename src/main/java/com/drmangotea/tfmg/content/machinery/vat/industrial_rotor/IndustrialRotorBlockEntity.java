package com.drmangotea.tfmg.content.machinery.vat.industrial_rotor;

import com.drmangotea.tfmg.TFMGRegistries;
import com.drmangotea.tfmg.content.machinery.vat.base.IVatMachine;
import com.drmangotea.tfmg.content.machinery.vat.base.VatBlock;
import com.drmangotea.tfmg.content.machinery.vat.base.VatBlockEntity;
import com.drmangotea.tfmg.content.machinery.vat.base.registry.operations.VatOperation;
import com.drmangotea.tfmg.content.machinery.vat.industrial_rotor.mode.RotorMode;
import com.drmangotea.tfmg.registry.TFMGBlockEntities;
import com.drmangotea.tfmg.registry.TFMGDataComponents;
import com.drmangotea.tfmg.registry.TFMGRotorModes;
import com.drmangotea.tfmg.registry.TFMGVatOperations;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.item.SmartInventory;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.Clearable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import java.util.List;

@MethodsReturnNonnullByDefault
public class IndustrialRotorBlockEntity extends KineticBlockEntity implements IVatMachine, Clearable {
    public DualSlotSmartInventory inventory = new DualSlotSmartInventory(2, this);
    public IItemHandlerModifiable itemCapability;

    public RotorMode rotorMode = TFMGRotorModes.NONE.get();
    public int vatSize = 1;
    public int vatHeight = 1;
    public BlockPos vatPos = null;
    public boolean updateVat = false;

    private static final float PULL_SPEED_DIP = 1f / 8f;
    private static final float PULL_SPEED_UP = 3f / 8f;

    LerpedFloat pullSpeed = LerpedFloat.linear();
    public float pulledAmount;
    private float pullTarget;
    private boolean pulling;

    public IndustrialRotorBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        this.itemCapability = inventory;
    }

    public ItemStack getRotorItem() {
        return inventory.getStackInSlot(0);
    }

    public ItemStack getSecondaryItem() {
        return inventory.getStackInSlot(1);
    }

    @Override
    public VatOperation getOperationId() {
        return rotorMode.getOperationId().get();
    }

    @Override
    public boolean canOperate(VatBlockEntity vat) {
        return Math.abs(getSpeed()) >= 3;
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null) return;
        if (level.isClientSide) {
            pullSpeed.updateChaseTarget(pullTarget);
            pullSpeed.tickChaser();
            return;
        }
        if (this.updateVat) {
            VatBlock.updateVatState(getBlockState(), level, getBlockPos().relative(Direction.DOWN));
            this.updateVat = false;
        }
        boolean nowPulling = isPulling();
        if (nowPulling != this.pulling) {
            this.pulling = nowPulling;
            sendData();
            setChanged();
        }
    }

    private boolean isPulling() {
        if (level == null || vatPos == null)
            return false;
        if (!(level.getBlockEntity(vatPos) instanceof VatBlockEntity vat))
            return false;
        return vat.recipe != null
                && vat.recipe.machines.stream().anyMatch(op -> op.equals(TFMGVatOperations.CRYSTAL_PULLER.get()));
    }

    public int getMaxPullDistance() {
        return vatHeight;
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return new AABB(getBlockPos()).inflate(3).expandTowards(0, -vatHeight - 1, 0);
    }

    @Override
    public void onSpeedChanged(float previous) {
        super.onSpeedChanged(previous);
        if (getSpeed() != previous) {
            notifyVatUpdate();
        }
    }

    @Override
    public void clearContent() {
        this.inventory.clearContent();
    }

    public void onInventoryChanged(int slot) {
        sendData();
        setChanged();

        if (this.inventory.isEmpty()) {
            this.rotorMode = TFMGRotorModes.NONE.get();
            notifyVatUpdate();
            return;
        }

        if (slot == 0) {
            ItemStack itemStack = this.inventory.getStackInSlot(0);
            this.rotorMode = itemStack.getOrDefault(TFMGDataComponents.ROTOR_MODE, RotorMode.Stored.NONE).mode().value();
            notifyVatUpdate();
        }
    }

    private void notifyVatUpdate() {
        this.updateVat = true;
    }

    @Override
    public void vatUpdated(VatBlockEntity be) {
        vatSize = be.getWidth();
        vatHeight = be.getHeight();
        vatPos = be.getBlockPos();
    }

    @Override
    public PositionRequirement getPositionRequirement() {
        return PositionRequirement.TOP_CENTER;
    }

    @Override
    public List<VatOperation> doesntWorkWith() {
        return List.of(TFMGVatOperations.ELECTRODE.get(), TFMGVatOperations.GRAPHITE_ELECTRODE.get());
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        compound.put("Inventory", inventory.serializeNBT(registries));
        compound.putFloat("PullTarget", pulling ? -PULL_SPEED_DIP : PULL_SPEED_UP);
        TFMGRegistries.ROTOR_MODE_REGISTRY.byNameCodec().encodeStart(NbtOps.INSTANCE, rotorMode).ifSuccess(nbt -> compound.put("Mode", nbt));
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        inventory.deserializeNBT(registries, compound.getCompound("Inventory"));
        if (compound.contains("PullTarget"))
            pullTarget = compound.getFloat("PullTarget");
        TFMGRegistries.ROTOR_MODE_REGISTRY.byNameCodec().parse(NbtOps.INSTANCE, compound.get("Mode")).ifSuccess(mode -> rotorMode = mode);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                TFMGBlockEntities.INDUSTRIAL_ROTOR.get(),
                (be, ctx) -> be.itemCapability
        );
    }

    public static class DualSlotSmartInventory extends SmartInventory {
        private final IndustrialRotorBlockEntity be;

        public DualSlotSmartInventory(int slots, IndustrialRotorBlockEntity be) {
            super(slots, be, 1, false);
            this.be = be;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }

            boolean isRotorModeItem = stack.has(TFMGDataComponents.ROTOR_MODE);
            int targetSlot = isRotorModeItem ? 0 : 1;

            if (!getStackInSlot(targetSlot).isEmpty()) {
                return stack;
            }

            if (!simulate) {
                super.insertItem(targetSlot, stack.copyWithCount(1), false);
            }

            ItemStack remainder = stack.copy();
            remainder.shrink(1);
            return remainder;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != 0 && slot != 1) {
                return ItemStack.EMPTY;
            }

            ItemStack current = getStackInSlot(slot);
            if (current.isEmpty()) {
                return ItemStack.EMPTY;
            }

            int extractCount = Math.min(amount, current.getCount());
            ItemStack extracted = current.copyWithCount(extractCount);

            if (!simulate) {
                ItemStack remaining = current.copy();
                remaining.shrink(extractCount);
                setStackInSlot(slot, remaining);
                be.onInventoryChanged(slot);
            }

            return extracted;
        }
    }
}
