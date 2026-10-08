package com.drmangotea.tfmg.content.electricity.experimental.content.power_transmission.transformers.small;

import com.drmangotea.tfmg.base.TFMGUtils;
import com.drmangotea.tfmg.base.lang.TFMGTexts;
import com.drmangotea.tfmg.content.electricity.base.IElectric;
import com.drmangotea.tfmg.content.electricity.base.VoltageAlteringBlockEntity;
import com.drmangotea.tfmg.base.blocks.TFMGHorizontalDirectionalBlock;
import com.drmangotea.tfmg.content.electricity.experimental.ElectricalProperties;
import com.drmangotea.tfmg.content.electricity.experimental.IRealisticElectric;
import com.drmangotea.tfmg.content.electricity.experimental.RealElectricNetworkManager;
import com.drmangotea.tfmg.content.electricity.experimental.RealElectricalNetwork;
import com.drmangotea.tfmg.content.electricity.experimental.simulation.Resistance;
import com.drmangotea.tfmg.registry.TFMGBlocks;
import com.drmangotea.tfmg.registry.TFMGDataComponents;
import com.drmangotea.tfmg.registry.TFMGItems;

import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;



public class TransformerBlockEntity extends SmartBlockEntity implements IRealisticElectric {
    boolean updateInFront = false;

    public ItemStack primaryCoil = ItemStack.EMPTY;
    public ItemStack secondaryCoil = ItemStack.EMPTY;

    public float coilRatio = 0;

    ElectricalProperties p;

    public float primaryCurrent = 0;

    public float secondaryCurrent = 0;

    public TransformerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        p = new TransformerProperties(getPos(), state.getValue(TFMGHorizontalDirectionalBlock.FACING));
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }

    @Override
    public void onUpdated() {

        RealElectricalNetwork network = RealElectricNetworkManager.getNetwork(level);

        double primaryPower = 0;
        double secondaryPower = 0;

        Resistance secondaryResistance = network.getResistance(getPos(), 0);
        if (secondaryResistance != null) {
            double resistance = secondaryResistance.resistance;
            double voltage = secondaryResistance.getVoltage(level);
            secondaryCurrent = (float) (voltage / resistance);
            secondaryPower = Math.pow(secondaryPower, 2) * resistance;
        }
        Resistance primaryResistance = network.getResistance(getPos(), 1);
        if (primaryResistance != null) {
            double resistance = primaryResistance.resistance;
            double voltage = primaryResistance.getVoltage(level);
            primaryCurrent = (float) (voltage / resistance);
            primaryPower = Math.pow(primaryCurrent, 2) * resistance;


            
        }





    }

    @Override
    public void destroy() {
        if (level == null) return;
        super.destroy();
        BlockPos pos = this.getBlockPos();
        if (!primaryCoil.isEmpty()) {
            ItemEntity item = new ItemEntity(level, pos.getX() + .5f, pos.getY() + .5f, pos.getZ() + .5f, primaryCoil);
            level.addFreshEntity(item);
        }
        if (!secondaryCoil.isEmpty()) {
            ItemEntity item = new ItemEntity(level, pos.getX() + .5f, pos.getY() + .5f, pos.getZ() + .5f, secondaryCoil);
            level.addFreshEntity(item);
        }
    }

    @Override
    public ElectricalProperties getProperties() {
        return p;
    }

    @Override
    public long getPos() {
        return getBlockPos().asLong();
    }

    public void updateCoils() {
		float primaryTurns = (float) primaryCoil.getOrDefault(TFMGDataComponents.COIL_TURNS, 0);
		float secondaryTurns = (float) secondaryCoil.getOrDefault(TFMGDataComponents.COIL_TURNS, 0);
        if(primaryTurns == 0 || secondaryTurns == 0) {
            coilRatio = 0;
		} else {
			coilRatio = secondaryTurns / primaryTurns;
		}
    }

    @Override
    public boolean makeMultimeterTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.makeMultimeterTooltip(tooltip, isPlayerSneaking);

        if (coilRatio!=0) {
            TFMGTexts.Multimeter.separator().forGoggles(tooltip);
            TFMGTexts.Multimeter.transformerRatio(coilRatio).forGoggles(tooltip, 1);
        }
        return true;
    }

    @Override
    public float resistance() {
        if (level == null || coilRatio == 0) return 0;
        Direction facing = getDirection();
        if (level.getBlockEntity(getBlockPos().relative(facing)) instanceof IElectric be
			&& be.getData().getId() != data.getId()
			&& be.hasElectricitySlot(facing.getOpposite())
		) {
			int count = getBlocksConnectedToNetworkCount(getControlledBlock().getData().getId());
			if(count == 0) return 0;
			return Math.max(be.getNetworkResistance()*count / coilRatio, 0);
        }
        return 0;
    }

    @Override
    public boolean hasElectricitySlot(Direction direction) {
        return direction == getBlockState().getValue(FACING).getClockWise();
    }
    @Override
    public void onNetworkChanged(int oldVoltage, float oldPower) {
        super.onNetworkChanged(oldVoltage, oldPower);
        if (oldVoltage != getData().getVoltage() || oldPower != getPowerUsage()) {
            updateInFront = true;
        }
        sendStuff();
        setChanged();
    }

    }


    @Override
    public Level getWorld() {
        return level;
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        if (!primaryCoil.isEmpty())
            compound.put("PrimaryCoil", primaryCoil.save(registries));
        if (!secondaryCoil.isEmpty())
            compound.put("SecondaryCoil", secondaryCoil.save(registries));

        compound.putFloat("CoilRation", coilRatio);

    }

    @Override
    public void remove() {
        super.remove();
        this.removeBlock();
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);

        if (compound.contains("PrimaryCoil")) {
            ItemStack.parse(registries, compound.getCompound("PrimaryCoil")).ifPresent(i -> primaryCoil = i);
        }
        if (compound.contains("SecondaryCoil")) {
            ItemStack.parse(registries, compound.getCompound("SecondaryCoil")).ifPresent(i -> secondaryCoil = i);
        }

        coilRatio = compound.getFloat("CoilRation");
    }

    public static List<Direction> getCoilDirections(Level level, BlockPos pos, BlockHitResult result) {
        Direction direction = level.getBlockState(pos).getValue(TFMGHorizontalDirectionalBlock.FACING);
        Collection<Direction> validDirections = new ArrayList<>();
        validDirections.add(direction.getClockWise());
        validDirections.add(direction.getCounterClockWise());


        return IPlacementHelper.orderedByDistance(pos, result.getLocation(), validDirections);

    }

    @OnlyIn(Dist.CLIENT)
    public static void tickOutliner() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || !(mc.hitResult instanceof BlockHitResult result))
            return;

        BlockPos pos = result.getBlockPos();

        if (!TFMGBlocks.TRANSFORMER.has(mc.level.getBlockState(pos)))
            return;

		ItemStack heldItem = mc.player.getMainHandItem();
        if (!(TFMGItems.ELECTROMAGNETIC_COIL.isIn(heldItem) || heldItem.is(Items.AIR)))
            return;

        Direction direction = mc.level.getBlockState(pos).getValue(TFMGHorizontalDirectionalBlock.FACING);

        Direction coilDirection = getCoilDirections(mc.level, pos, result).getFirst();
        /////////

        Vec3 center = VecHelper.getCenterOf(pos);

        Vec3 corner1 = center.relative(coilDirection, 7 / 16f).relative(direction, 3 / 16f).relative(Direction.UP, 5.75 / 16f);
        Vec3 corner2 = center.relative(coilDirection, 1 / 16f).relative(direction.getOpposite(), 3 / 16f).relative(Direction.DOWN, 1.75 / 16f);

        TFMGUtils.createOutline(corner1, corner2, "CoilOutline", Color.rainbowColor(AnimationTickHolder.getTicks() * 5));
    }
}
