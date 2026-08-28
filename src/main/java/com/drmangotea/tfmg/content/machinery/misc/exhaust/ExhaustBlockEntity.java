package com.drmangotea.tfmg.content.machinery.misc.exhaust;

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
import net.minecraft.util.RandomSource;
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

public class ExhaustBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    protected IFluidHandler fluidCapability;
    public FluidTank tankInventory;

    public boolean spawnsSmoke = false;
    public int smokeTimer = 0;

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
        if (level == null) return;
        level.invalidateCapabilities(getBlockPos());

        Direction direction = this.getBlockState().getValue(ExhaustBlock.FACING);

        if(smokeTimer != 0) {
            spawnsSmoke = true;
            smokeTimer--;
        } else spawnsSmoke = false;

        if (spawnsSmoke) {
            switch (direction) {
                case UP -> makeParticles(level, this.getBlockPos(), 0);
                case DOWN -> makeParticles(level, this.getBlockPos(), 1);
                case NORTH -> makeParticles(level, this.getBlockPos(), 2);
                case SOUTH -> makeParticles(level, this.getBlockPos(), 3);
                case WEST -> makeParticles(level, this.getBlockPos(), 4);
                case EAST -> makeParticles(level, this.getBlockPos(), 5);
            }
        }

        if (tankInventory.getFluidAmount() > 0) {
            smokeTimer = 100;
            spawnsSmoke = true;
            tankInventory.drain(100, IFluidHandler.FluidAction.EXECUTE);
        }

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

        RandomSource random = level.getRandom();
        if (random.nextInt(7) != 0) return;

        double offX, offY, offZ;
        switch (particleRotation) {
            case 0 -> { offX = random.nextFloat() * 0.3f; offY = 1;                    offZ = random.nextFloat() * 0.3f; }
            case 1 -> { offX = random.nextFloat();        offY = 0;                    offZ = random.nextFloat();        }
            case 2 -> { offX = random.nextFloat();        offY = random.nextFloat();   offZ = 0;                        }
            case 3 -> { offX = random.nextFloat();        offY = random.nextFloat();   offZ = 1;                        }
            case 4 -> { offX = 1;                         offY = random.nextFloat();   offZ = random.nextFloat();        }
            case 5 -> { offX = 0;                         offY = random.nextFloat();   offZ = random.nextFloat();        }
            default -> { return; }
        }

        level.addParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,
                center.x + offX, center.y + offY, center.z + offZ,
                0.0D, 0.08D, 0.0D);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

}

