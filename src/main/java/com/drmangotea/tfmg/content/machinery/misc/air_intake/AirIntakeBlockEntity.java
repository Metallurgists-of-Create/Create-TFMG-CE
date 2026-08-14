package com.drmangotea.tfmg.content.machinery.misc.air_intake;

import com.drmangotea.tfmg.base.TFMGUtils;
import com.drmangotea.tfmg.base.lang.TFMGTexts;
import com.drmangotea.tfmg.registry.TFMGBlockEntities;
import com.drmangotea.tfmg.registry.TFMGFluids;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.fluid.SmartFluidTank;


import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;


import java.util.ArrayList;
import java.util.List;

import static com.drmangotea.tfmg.content.machinery.misc.air_intake.AirIntakeBlock.INVISIBLE;
import static com.simibubi.create.content.kinetics.base.DirectionalKineticBlock.FACING;

public class AirIntakeBlockEntity extends KineticBlockEntity implements IWrenchable {

    int diameter = 1;
    boolean isController = false;
    public boolean hasShaft = true;
    boolean isUsedByController = false;
    public BlockPos controller;
    public List<AirIntakeBlockEntity> blockEntities = new ArrayList<>();
    public float maxShaftSpeed = 0;
    public float angle = 0;
    public LerpedFloat visual_angle = LerpedFloat.angular();

    protected FluidTank tankInventory;
    protected IFluidHandler fluidCapability;


    public AirIntakeBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        tankInventory = createInventory();
        fluidCapability = tankInventory;


    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                TFMGBlockEntities.AIR_INTAKE.get(),
                (be, context) -> be.fluidCapability
        );
    }

    public int getProduction() {
        int production = ((int) maxShaftSpeed * ((diameter * diameter))) / 40;
        if (controller == null) {
            return Math.min(production, tankInventory.getSpace());
        }
        if (level != null && level.getBlockEntity(controller) instanceof AirIntakeBlockEntity intake) {
            production = ((int) intake.maxShaftSpeed * (intake.diameter * intake.diameter)) / 40;
        }
        return Math.min(production, tankInventory.getSpace());
    }

    public int getMinimumSpeed() {
        int rpm = (int) Math.ceil(40.0 / (diameter * diameter));
        if (controller == null) {
            return rpm;
        }
        if (level != null && level.getBlockEntity(controller) instanceof AirIntakeBlockEntity intake) {
            rpm = (int) Math.ceil(40.0 / (intake.diameter * intake.diameter));
        }
        return rpm;
    }

    public void tick(){
        super.tick();
        if (level == null) return;
        if (tankInventory.getFluidAmount() + getProduction() <= tankInventory.getCapacity()) {
            tankInventory.fill(new FluidStack(FluidHelper.convertToStill(TFMGFluids.AIR.get()), getProduction()), IFluidHandler.FluidAction.EXECUTE);
        }
        if(isUsedByController) {
            refreshCapability();
            sendData();
            setChanged();
        }
        if(diameter == 3){
            visual_angle.chase(angle, 0.1f, LerpedFloat.Chaser.EXP);
            visual_angle.tickChaser();
        }
        angle+=maxShaftSpeed/2;
        angle %= 360;
        if(isUsedByController)
            blockEntities.clear();
        if(!this.getBlockState().getValue(INVISIBLE)){
            if(isController||isUsedByController){
                level.setBlock(this.getBlockPos(),this.getBlockState().setValue(INVISIBLE,true),2);
            }
        }
        if(!isController&&!isUsedByController)
            level.setBlock(this.getBlockPos(),this.getBlockState().setValue(INVISIBLE,false),2);
        if(controller == null)
            controller = this.getBlockPos();
        diameter = getPossibleDiameter();
        if(controller == this.getBlockPos()) {
            isUsedByController = false;
        } else {
            isUsedByController = true;
            isController = false;
        }
        if(diameter ==1) {
            isController = false;
        }
        BlockEntity controllerEntity = controller != null ? level.getBlockEntity(controller) : null;
        if (controllerEntity instanceof AirIntakeBlockEntity controllerIntake) {
            if (!controllerIntake.isController) {
                isUsedByController = false;
            }
            if (controllerIntake.diameter == 2) {
                int x = Math.abs(this.getBlockPos().getX() - controller.getX());
                int y = Math.abs(this.getBlockPos().getY() - controller.getY());
                int z = Math.abs(this.getBlockPos().getZ() - controller.getZ());

                if (x > 1 || y > 1 || z > 1) {
                    isUsedByController = false;
                    controller = this.getBlockPos();
                }
            }
            if(controllerIntake.diameter==1) {
                isUsedByController = false;
                controller = this.getBlockPos();
            }
        } else {
            isUsedByController = false;
            controller = this.getBlockPos();
        }
        if(diameter == 1){
            maxShaftSpeed = Math.abs(getSpeed());
        }else {
            maxShaftSpeed = Math.abs(getSpeed());
            List<Float> speeds = new ArrayList<>();

            for (AirIntakeBlockEntity be : blockEntities) {
                speeds.add(Math.abs(be.getSpeed()));
            }

            for(float testedSpeed : speeds){
                if(testedSpeed> maxShaftSpeed)
                    maxShaftSpeed = testedSpeed;
            }
        }
        if(isUsedByController)
            return;
        if(diameter ==2){
            if(blockEntities.toArray().length != 4)
                return;
        }
        if(diameter == 3){
            if(blockEntities.toArray().length != 9)
                return;
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        invalidateCapabilities();
    }

    public InteractionResult onWrenched(BlockState state, UseOnContext context){
        Direction direction = context.getClickedFace();
        if(direction == getBlockState().getValue(FACING).getOpposite()) {
            hasShaft = !hasShaft;
        }
        return InteractionResult.SUCCESS;
    }

    public void setController(BlockPos controllerPos) {
        controller  = controllerPos;
    }

    private void refreshCapability() {
        IFluidHandler handlerForCapability;
        if (level == null) return;
        if (controller == null || controller == this.getBlockPos()) {
            handlerForCapability = tankInventory;
        } else if (level.getBlockEntity(controller) instanceof AirIntakeBlockEntity intakeController) {
            handlerForCapability = intakeController.tankInventory;
        } else
            handlerForCapability = tankInventory;
        fluidCapability = handlerForCapability;
    }

    public int getPossibleDiameter() {
        if(controller != this.getBlockPos())
            return 1;
        if (level == null) return 0;

        BlockPos checkedPos;
        Direction direction = this.getBlockState().getValue(FACING);
        List<BlockPos> checkedPosses = new ArrayList<>();
        checkedPos = this.getBlockPos();
        boolean canBeMedium = true;
        for(int x = 0;x < 2; x++){
            for(int z = 0;z < 2; z++){
                checkedPosses.add(checkedPos);
                if(direction.getAxis().isHorizontal()) {
                    checkedPos = checkedPos.above();
                } else checkedPos = checkedPos.east();
            }
            if (direction.getAxis().isHorizontal()) {
                checkedPos = checkedPos.below(2);
                checkedPos = checkedPos.relative(direction.getClockWise());
            } else {
                checkedPos = checkedPos.west(2);
                checkedPos = checkedPos.south();
            }
        }
        List<BlockPos> checkedPossesLarge = new ArrayList<>();
        checkedPos = this.getBlockPos();
        boolean canBeLarge = true;
        for (int x = 0;x < 3; x++){
            for (int z = 0;z < 3; z++){
                checkedPossesLarge.add(checkedPos);
                if(direction.getAxis().isHorizontal()) {
                    checkedPos = checkedPos.above();
                }else checkedPos = checkedPos.east();
            }
            if (direction.getAxis().isHorizontal()) {
                checkedPos = checkedPos.below(3);
                checkedPos = checkedPos.relative(direction.getClockWise());
            } else {
                checkedPos = checkedPos.west(3);
                checkedPos = checkedPos.south();

            }
        }
        //LARGE
        for(BlockPos pos : checkedPossesLarge){
            if (!(level.getBlockEntity(pos) instanceof AirIntakeBlockEntity intake)) {
                canBeLarge = false;
                break;
            }
            if (intake.getBlockState().getValue(FACING) != this.getBlockState().getValue(FACING)) {
                canBeLarge = false;
                break;
            }
        }
        //MEDIUM
        for(BlockPos pos : checkedPosses){
            if (!(level.getBlockEntity(pos) instanceof AirIntakeBlockEntity intake)) {
                canBeMedium = false;
                break;
            }
            if (pos!=this.getBlockPos())
                if(intake.isController) {
                    canBeMedium = false;
                    break;
                }
            if (intake.getBlockState().getValue(FACING) != this.getBlockState().getValue(FACING)) {
                canBeMedium = false;
                break;
            }
        }
        if(canBeLarge) {
            this.blockEntities.clear();
            for (BlockPos pos : checkedPossesLarge) {
                if (level.getBlockEntity(pos) instanceof AirIntakeBlockEntity intake) {
                    if (intake.isUsedByController && intake.controller != this.getBlockPos() && pos != this.getBlockPos() || isController) {
                        intake.isUsedByController = true;
                        intake.isController = false;
                        intake.controller =this.getBlockPos();
                    }
                    intake.setController(this.getBlockPos());
                    this.blockEntities.add(intake);
                }
            }
            controller = this.getBlockPos();
            isController = true;
            return 3;
        }
        if(canBeMedium) {
            this.blockEntities.clear();
            for (BlockPos pos : checkedPosses) {
                if (level.getBlockEntity(pos) instanceof AirIntakeBlockEntity intake) {
                    if (intake.isUsedByController && intake.controller != this.getBlockPos() && pos != this.getBlockPos()) {
                        controller = this.getBlockPos();
                        isController = false;
                        return 1;
                    }
                    intake.setController(this.getBlockPos());
                    this.blockEntities.add(intake);
                }
            }
            controller = this.getBlockPos();
            isController = true;
            return 2;
        }
        controller = this.getBlockPos();
        isController = false;
        return 1;
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return new AABB(this.getBlockPos()).inflate(3);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (getProduction() == 0) {
            TFMGTexts.CommonMachines.minRPM(getMinimumSpeed()).style(ChatFormatting.RED).forGoggles(tooltip);
        }
        TFMGTexts.fluidProduction(getProduction()).style(getProduction() > 0 ? ChatFormatting.AQUA : ChatFormatting.RED).forGoggles(tooltip);
        TFMGUtils.createFluidTooltip(this, tooltip);
        return true;
    }

    protected SmartFluidTank createInventory() {
        return new SmartFluidTank(8000, this::onFluidStackChanged) {
            @Override
            public boolean isFluidValid(@NotNull FluidStack stack) {
                return stack.getFluid().isSame(TFMGFluids.AIR.getSource());
            }
        };
    }

    protected void onFluidStackChanged(FluidStack newFluidStack) {
            setChanged();
            sendData();
    }


    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound,registries , clientPacket);
        diameter = compound.getInt("Diameter");
        isController = compound.getBoolean("IsController");
        isUsedByController = compound.getBoolean("IsUsed");
        hasShaft = compound.getBoolean("HasShaft");
        tankInventory.readFromNBT(registries,compound.getCompound("TankContent"));
    }

    @Override
    public void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound,registries , clientPacket);
        compound.putInt("Diameter", diameter);
        compound.putBoolean("IsController", isController);
        compound.putBoolean("IsUsed", isUsedByController);
        compound.putBoolean("HasShaft", hasShaft);
        compound.put("TankContent", tankInventory.writeToNBT(registries,new CompoundTag()));
    }
}
