package com.drmangotea.tfmg.content.machinery.oil_processing.pumpjack.hammer;

import com.drmangotea.tfmg.content.machinery.oil_processing.pumpjack.PumpjackMultiblock;
import com.drmangotea.tfmg.content.machinery.oil_processing.pumpjack.base.PumpjackBaseBlockEntity;
import com.drmangotea.tfmg.content.machinery.oil_processing.pumpjack.crank.PumpjackCrankBlockEntity;
import com.drmangotea.tfmg.registry.TFMGTags;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.content.contraptions.ControlledContraptionEntity;
import com.simibubi.create.content.contraptions.IDisplayAssemblyExceptions;
import com.simibubi.create.content.contraptions.bearing.IBearingBlockEntity;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.transmission.sequencer.SequencerInstructions;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import net.createmod.catnip.math.AngleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;

import java.util.List;

import static com.drmangotea.tfmg.content.machinery.oil_processing.pumpjack.hammer.PumpjackBlock.WIDE;

public class PumpjackBlockEntity extends GeneratingKineticBlockEntity implements IBearingBlockEntity, IDisplayAssemblyExceptions {
    protected ControlledContraptionEntity movedContraption;
    protected float angle;
    protected boolean running;
    protected boolean assembleNextTick;
    protected float clientAngleDiff;
    protected AssemblyException lastException;
    protected double sequencedAngleLimit;
    private float prevAngle;
	public final PumpjackMultiblock multiblock;

    public PumpjackBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
		multiblock = new PumpjackMultiblock(this);
        setLazyTickRate(3);
        sequencedAngleLimit = -1;
    }

    @Override
    public boolean isWoodenTop() {
        return false;
    }

    @Override
    protected boolean syncSequenceContext() {
        return true;
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return super.createRenderBoundingBox().inflate(7);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);

        registerAwardables(behaviours, AllAdvancements.CONTRAPTION_ACTORS);
    }

    @Override
    public void remove() {
        if (!level.isClientSide)
            disassemble();
        super.remove();
    }

    @Override
    public void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        compound.putBoolean("Running", running);
        compound.putFloat("Angle", angle);
        if (sequencedAngleLimit >= 0)
            compound.putDouble("SequencedAngleLimit", sequencedAngleLimit);
        AssemblyException.write(compound,registries, lastException);
		compound.put("MultiblockData", multiblock.write(registries, clientPacket));
        super.write(compound,registries , clientPacket);
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        if (wasMoved) {
            super.read(compound,registries , clientPacket);
            return;
        }
        float angleBefore = angle;
        running = compound.getBoolean("Running");
        angle = compound.getFloat("Angle");
        sequencedAngleLimit = compound.contains("SequencedAngleLimit") ? compound.getDouble("SequencedAngleLimit") : -1;
        lastException = AssemblyException.read(compound,registries);
		multiblock.read(compound.getCompound("MultiblockData"), registries, clientPacket);
        super.read(compound,registries , clientPacket);
        if (!clientPacket)
            return;
        if (running) {
            if (movedContraption == null || !movedContraption.isStalled()) {
                clientAngleDiff = AngleHelper.getShortestAngleDiff(angleBefore, angle);
                angle = angleBefore;
            }
        } else
            movedContraption = null;
    }

    @Override
    public float getInterpolatedAngle(float partialTicks) {
        if (isVirtual())
            return Mth.lerp(partialTicks + .5f, prevAngle, angle);
        if (movedContraption == null || movedContraption.isStalled() || !running)
            partialTicks = 0;
        float angularSpeed = getAngularSpeed();
        if (sequencedAngleLimit >= 0)
            angularSpeed = (float) Mth.clamp(angularSpeed, -sequencedAngleLimit, sequencedAngleLimit);
        return Mth.lerp(partialTicks, angle, angle + angularSpeed);
    }

    @Override
    public void onSpeedChanged(float prevSpeed) {
        super.onSpeedChanged(prevSpeed);
        assembleNextTick = true;
        sequencedAngleLimit = -1;
        if (movedContraption != null && Math.signum(prevSpeed) != Math.signum(getSpeed()) && prevSpeed != 0) {
            if (!movedContraption.isStalled()) {
                angle = Math.round(angle);
                applyRotation();
            }
            movedContraption.getContraption()
                    .stop(level);
        }
        if (sequenceContext != null
                && sequenceContext.instruction() == SequencerInstructions.TURN_ANGLE)
            sequencedAngleLimit = sequenceContext.getEffectiveValue(getTheoreticalSpeed());
    }

    public float getAngularSpeed() {
        float speed = convertToAngular(getSpeed());
        if (getSpeed() == 0)
            speed = 0;
        if (level != null && level.isClientSide) {
            speed *= ServerSpeedProvider.get();
            speed += clientAngleDiff / 3f;
        }
        return speed;
    }

    @Override
    public AssemblyException getLastAssemblyException() {
        return lastException;
    }

    @Override
    public BlockPos getBlockPosition() {
        return worldPosition;
    }

    public void assemble() {
        if (!(getBlockState().getBlock() instanceof PumpjackBlock)) //is this even necessary, though?
            return;
        Direction direction = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        PumpjackContraption contraption = new PumpjackContraption(direction);
		BlockPos headPos = multiblock.getHead();
		BlockPos connectorPos = multiblock.getConnector();
        try {
            if (!contraption.assemble(level, worldPosition))
                return;
            if (connectorPos == null || headPos == null)
                return;
            lastException = null;
        } catch (AssemblyException e) {
            lastException = e;
            sendData();
            return;
        }
        int q = (direction.getAxis() == Direction.Axis.X)? -1 : 1;
        boolean canAssemble = true;
        boolean foundHead = false;
        boolean foundConnector = false;
        BlockPos headLocalPos = headPos.subtract(getBlockPos().above());
        for (StructureTemplate.StructureBlockInfo block : contraption.getBlocks().values()) {
            if (block.state().is(TFMGTags.Blocks.PUMPJACK_HEAD.tag)) {
                foundHead = true;
                if (block.pos().getX() != headLocalPos.getX() ||
                    block.pos().getY() != q * headLocalPos.getY() ||
                    block.pos().getZ() != q * headLocalPos.getZ()
				) { canAssemble = false; }
            }
        }
        BlockPos connectorLocalPos = connectorPos.subtract(getBlockPos().above());
        for (StructureTemplate.StructureBlockInfo block : contraption.getBlocks().values()) {
            if (block.state().is(TFMGTags.Blocks.PUMPJACK_CONNECTOR.tag)) {
                foundConnector = true;
                if (block.pos().getX() != connectorLocalPos.getX() ||
                        block.pos().getY() != q * connectorLocalPos.getY() ||
                        block.pos().getZ() != q * connectorLocalPos.getZ())
                    canAssemble = false;
            }
        }
        if (!canAssemble || !foundHead || !foundConnector)
            return;
		PumpjackBaseBlockEntity baseBE = multiblock.getBaseBE();
        if (baseBE != null && baseBE.controllerHammer != this && baseBE.controllerHammer != null)
            return;
        contraption.removeBlocksFromWorld(level, BlockPos.ZERO);
        movedContraption = ControlledContraptionEntity.create(level, this, contraption);
        BlockPos anchor = worldPosition.above();
        movedContraption.setPos(anchor.getX(), anchor.getY(), anchor.getZ());
        movedContraption.setRotationAxis(direction.getClockWise().getAxis());
        level.addFreshEntity(movedContraption);
        AllSoundEvents.MECHANICAL_PRESS_ACTIVATION.playOnServer(level, worldPosition);
        if (contraption.containsBlockBreakers())
            award(AllAdvancements.CONTRAPTION_ACTORS);
        running = true;
        angle = 0;
        sendData();
        updateGeneratedRotation();
    }

    public void disassemble() {
        if (!running && movedContraption == null)
            return;
        multiblock.connectorDistance = 0;
		multiblock.headDistance = 0;
        angle = 0;
        sequencedAngleLimit = -1;
        if (movedContraption != null) {
            movedContraption.disassemble();
            AllSoundEvents.MECHANICAL_PRESS_ACTIVATION.playOnServer(level, worldPosition);
        }
        movedContraption = null;
        running = false;
        updateGeneratedRotation();
        assembleNextTick = false;
        sendData();
    }

    private int incompleteTimer = 0;

    @Override
    public void tick() {
        super.tick();
        if (level == null) return;
        if (!running) {
			multiblock.findHeadAndConnector();
			sendData();
        }
		if (!running && multiblock.isComplete() && !level.isClientSide && assembleNextTick) {
			assemble();
		}
		PumpjackBaseBlockEntity base = multiblock.getBaseBE();
		if (running && base != null && base.controllerHammer == null) {
			base.setControllerHammer(this);
		}
        boolean actuallyComplete = multiblock.isComplete();
        if (actuallyComplete) {
            incompleteTimer = 0;
        } else {
            incompleteTimer++;
        }
        if (incompleteTimer > 40)
            if (!level.isClientSide)
                disassemble();

        setHolderSize();
		multiblock.evaluate();
        prevAngle = angle;
        if (level.isClientSide)
            clientAngleDiff /= 2;
        if (!level.isClientSide && assembleNextTick) {
            assembleNextTick = false;
            if (!running) {
                assemble();
            }
        }
        if (!running)
            return;
        if (!(movedContraption != null && movedContraption.isStalled())) {
			PumpjackCrankBlockEntity Crank = multiblock.getCrankBE();
            if (Crank != null) {
                angle = (float) Math.toDegrees(Math.atan(Crank.heightModifier / multiblock.connectorDistance));
            }
        }
        applyRotation();
    }

    public void setHolderSize() {
        if (isRunning() || level == null)
            return;

		boolean small = level.getBlockState(getBlockPos().above()).is(TFMGTags.Blocks.PUMPJACK_SMALL_PART.tag);
		boolean wide = getBlockState().getValue(WIDE);

        if (!small && !wide)
            level.setBlock(getBlockPos(), getBlockState().setValue(WIDE, true), 2);

        if (small && wide)
            level.setBlock(getBlockPos(), getBlockState().setValue(WIDE, false), 2);
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (movedContraption != null && level != null && !level.isClientSide)
            sendData();
    }

    protected void applyRotation() {
        if (movedContraption == null)
            return;
        movedContraption.setAngle(angle);
        if (getBlockState().hasProperty(BlockStateProperties.HORIZONTAL_FACING))
            movedContraption.setRotationAxis(getBlockState()
				.getValue(BlockStateProperties.HORIZONTAL_FACING)
				.getClockWise()
				.getAxis()
			);
    }

    @Override
    public void attach(ControlledContraptionEntity contraption) {
        BlockState blockState = getBlockState();
        if (!(contraption.getContraption() instanceof PumpjackContraption))
            return;
        if (!blockState.hasProperty(BlockStateProperties.HORIZONTAL_FACING))
            return;
        this.movedContraption = contraption;
        setChanged();
        BlockPos anchor = worldPosition.above();
        movedContraption.setPos(anchor.getX(), anchor.getY(), anchor.getZ());
        if (level != null && !level.isClientSide) {
            this.running = true;
            sendData();
        }
    }

    @Override
    public void onStall() {
        if (level != null && !level.isClientSide)
            sendData();
    }

    @Override
    public boolean isValid() {
        return !isRemoved();
    }

    @Override
    public boolean isAttachedTo(AbstractContraptionEntity contraption) {
        return movedContraption == contraption;
    }

    public boolean isRunning() {
        return running;
    }

    public void setAngle(float forcedAngle) {
        angle = forcedAngle;
    }
}
