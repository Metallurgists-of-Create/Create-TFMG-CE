package com.drmangotea.tfmg.content.machinery.misc.exhaust;


import com.drmangotea.tfmg.TFMG;
import com.drmangotea.tfmg.base.TFMGUtils;
import com.drmangotea.tfmg.registry.TFMGBlockEntities;
import com.drmangotea.tfmg.registry.TFMGFluids;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.List;
import java.util.Random;

@SuppressWarnings("removal")
public class ExhaustBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {

    protected IFluidHandler fluidCapability;
    public FluidTank tankInventory;

    public boolean spawnsSmoke=false;
    public int smokeTimer=0;

    public ExhaustBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        tankInventory = createInventory();
        fluidCapability = tankInventory;

    }
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                TFMGBlockEntities.EXHAUST.get(),
                (be, context) -> be.fluidCapability
        );
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return TFMGUtils.createFluidTooltip(this, tooltip);
    }

    protected SmartFluidTank createInventory() {
        return new SmartFluidTank(1000, this::onFluidStackChanged) {
            @Override
            public boolean isFluidValid(FluidStack stack) {
                return stack.getFluid().isSame(TFMGFluids.CARBON_DIOXIDE.getSource());
            }
        };
    }

    protected void onFluidStackChanged(FluidStack newFluidStack) {
        sendData();
        setChanged();
    }

    @Override
    public void tick() {
        super.tick();
        Direction direction = this.getBlockState().getValue(ExhaustBlock.FACING);

        if(smokeTimer != 0) {
            spawnsSmoke = true;
            smokeTimer--;
        } else spawnsSmoke = false;

        switch (direction) {
            case UP -> {if(spawnsSmoke) makeParticles(level, this.getBlockPos(), 0);}
            case DOWN -> {if(spawnsSmoke) makeParticles(level, this.getBlockPos(), 1);}
            case NORTH -> {if(spawnsSmoke) makeParticles(level, this.getBlockPos(), 2);}
            case SOUTH -> {if(spawnsSmoke) makeParticles(level, this.getBlockPos(), 3);}
            case EAST -> {if(spawnsSmoke) makeParticles(level, this.getBlockPos(), 4);}
            case WEST -> {if(spawnsSmoke) makeParticles(level, this.getBlockPos(), 5);}
        }

        if(tankInventory.getFluidAmount() > 0) {
            smokeTimer = 100;
            spawnsSmoke = true;
        }

        if (tankInventory.getSpace() > 700) {
            tankInventory.drain(100, IFluidHandler.FluidAction.EXECUTE);
        } else tankInventory.drain(10, IFluidHandler.FluidAction.EXECUTE);
    }



    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound,registries , clientPacket);
        tankInventory.readFromNBT(registries,compound.getCompound("TankContent"));
        smokeTimer = compound.getInt("Timer");
    }

    @Override
    public void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound,registries , clientPacket);
        compound.put("TankContent", tankInventory.writeToNBT(registries,new CompoundTag()));
        compound.putInt("Timer", smokeTimer);
    }

    public void makeParticles(Level level, BlockPos pos, int particleRotation) {
        SubLevelAccess subLevel = SableCompanion.INSTANCE.getContaining(this);
        Vec3 center = pos.getCenter();

        if (subLevel != null) {
            center = subLevel.logicalPose().transformPosition(center);
        }

        Random random = TFMG.RANDOM;
        int shouldSpawnSmoke = random.nextInt(7);
        if(shouldSpawnSmoke == 0) {
            if(particleRotation==0)
                level.addParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, center.x + random.nextFloat(0.3f), center.y + 1, center.z + random.nextFloat(0.3f), 0.0D, 0.08D, 0.0D);
            if(particleRotation==1)
                level.addParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, center.x + random.nextFloat(1), center.y, center.z + random.nextFloat(1), 0.0D, 0.08D, 0.0D);
            if(particleRotation==2)
                level.addParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, center.x + random.nextFloat(1), center.y + random.nextFloat(1), center.z, 0.0D, 0.08D, 0.0D);
            if(particleRotation==3)
                level.addParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, center.x + random.nextFloat(1), center.y + random.nextFloat(1), center.z + 1, 0.0D, 0.08D, 0.0D);
            if(particleRotation==4)
                level.addParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, center.x + 1, center.y + random.nextFloat(1), center.z + random.nextFloat(1), 0.0D, 0.08D, 0.0D);
            if(particleRotation==5)
                level.addParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, center.x, center.y + random.nextFloat(1), center.z + random.nextFloat(1), 0.0D, 0.08D, 0.0D);
        }

    }

    //@Nonnull
    //@Override
    //public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
//
    //    if (cap == ForgeCapabilities.FLUID_HANDLER)
    //        return fluidCapability.cast();
    //    return super.getCapability(cap, side);
    //}



    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}



}

