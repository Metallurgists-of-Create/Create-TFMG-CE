package com.drmangotea.tfmg.content.machinery.misc.flarestack;


import com.drmangotea.tfmg.TFMG;
import com.drmangotea.tfmg.base.TFMGUtils;
import com.drmangotea.tfmg.registry.TFMGBlockEntities;
import com.drmangotea.tfmg.registry.TFMGTags;
import com.simibubi.create.Create;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.fluid.SmartFluidTank;


import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.List;
import java.util.Random;

public class FlarestackBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {

    public FluidTank tankInventory;
    public int smokeTimer = 0;


    public FlarestackBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        tankInventory = createInventory();
    }

    protected SmartFluidTank createInventory() {
        return new SmartFluidTank(2500, this::onFluidStackChanged) {
            @Override
            public boolean isFluidValid(FluidStack stack) {
                return stack.getFluid().is(TFMGTags.TFMGFluidTags.FLAMMABLE.tag)||
                        stack.getFluid().is(TFMGTags.TFMGFluidTags.FUEL.tag);
            }
        };
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                TFMGBlockEntities.FLARESTACK.get(),
                (be, context) -> be.tankInventory
        );
    }

    @Override
    public void invalidate() {
        super.invalidate();
        invalidateCapabilities();
    }

    protected void onFluidStackChanged(FluidStack newFluidStack) {
        if (!hasLevel()) return;
        sendData();
        setChanged();
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return TFMGUtils.createFluidTooltip(this, tooltip);
    }



    @Override
    public void tick() {
        super.tick();

        if (tankInventory.isEmpty() || !tankInventory.isFluidValid(tankInventory.getFluid())) {
            level.setBlock(getBlockPos(), this.getBlockState().setValue(FlarestackBlock.LIT, false), 2);
            return;
        }

        if (tankInventory.getFluidAmount() > 0 && smokeTimer < 97) {
            if(tankInventory.getFluidAmount() > 1000) {
                tankInventory.drain(100, IFluidHandler.FluidAction.EXECUTE);
            } else
                tankInventory.drain(30, IFluidHandler.FluidAction.EXECUTE);
            smokeTimer = 100;
        }

        if (smokeTimer > 0) {
            smokeTimer--;
            makeParticles(level, this.getBlockPos());
            level.setBlock(getBlockPos(), this.getBlockState().setValue(FlarestackBlock.LIT, true), 2);
        }
    }

    public static void makeParticles(Level level, BlockPos pos) {
        Random random = TFMG.RANDOM;
        int shouldSpawnSmoke = random.nextInt(7);
        if(shouldSpawnSmoke==0) {
            level.addParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, pos.getX()  +random.nextFloat(1), pos.getY() + 1, pos.getZ()  +random.nextFloat(1), 0.0D, 0.08D, 0.0D);
            level.addParticle(ParticleTypes.FLAME, pos.getX()  +random.nextFloat(1), pos.getY() + 1, pos.getZ()  +random.nextFloat(1), TFMG.RANDOM.nextDouble(0.28)-0.14D, 0.14D, TFMG.RANDOM.nextDouble(0.28)-0.14D);
            level.addParticle(ParticleTypes.FLAME, pos.getX()  +random.nextFloat(1), pos.getY() + 1, pos.getZ()  +random.nextFloat(1), TFMG.RANDOM.nextDouble(0.28)-0.14D, 0.14D, TFMG.RANDOM.nextDouble(0.28)-0.14D);
            level.addParticle(ParticleTypes.FLAME, pos.getX()  +random.nextFloat(1), pos.getY() + 1, pos.getZ()  +random.nextFloat(1), TFMG.RANDOM.nextDouble(0.28)-0.14D, 0.14D, TFMG.RANDOM.nextDouble(0.28)-0.14D);
        }
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound,registries , clientPacket);
        tankInventory.readFromNBT(registries,compound.getCompound("TankContent"));
    }

    @Override
    public void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound,registries , clientPacket);
        compound.put("TankContent", tankInventory.writeToNBT(registries,new CompoundTag()));
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }
}

