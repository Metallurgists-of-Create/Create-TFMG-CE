package com.drmangotea.tfmg.content.machinery.metallurgy.coke_oven;

import com.drmangotea.tfmg.base.TFMGUtils;
import com.drmangotea.tfmg.base.lang.TFMGTexts;
import com.drmangotea.tfmg.config.TFMGConfigs;
import com.drmangotea.tfmg.recipes.CokingRecipe;
import com.drmangotea.tfmg.registry.TFMGBlockEntities;
import com.drmangotea.tfmg.registry.TFMGBlocks;
import com.drmangotea.tfmg.registry.TFMGRecipeTypes;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.item.SmartInventory;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

import java.util.List;

import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;

public class CokeOvenBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {

    public SmartInventory inventory;
    public FluidTank primaryTank;
    public FluidTank secondaryTank;
    protected IFluidHandler primaryFluidCapability;
    protected IFluidHandler secondaryFluidCapability;
    public IItemHandlerModifiable itemCapability;
    public LerpedFloat doorAngle = LerpedFloat.angular();
    public boolean createNextTick;
    public BlockPos controller = getBlockPos();
    public int size = 1;
    public boolean forceOpen = false;

    int totalTime = 0;
    int timer = 0;
    private final RecipeManager.CachedCheck<RecipeWrapper, CokingRecipe> quickCheck;

    public CokeOvenBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setLazyTickRate(10);
        inventory = new SmartInventory(1, this)
                .withMaxStackSize(64)
                .whenContentsChanged(i->this.onContentsChanged());
        primaryTank = new SmartFluidTank(8000, this::onFluidChanged);
        secondaryTank = new SmartFluidTank(8000, this::onFluidChanged);
        itemCapability = inventory;
        primaryFluidCapability =  primaryTank;
        secondaryFluidCapability = secondaryTank;
        createNextTick = true;

        this.quickCheck = RecipeManager.createCheck(TFMGRecipeTypes.COKING.getType());
    }

    public void onContentsChanged(){
        if(!inventory.isEmpty() && timer == 0){
            executeRecipe();
        }
        if(inventory.isEmpty()) {
            totalTime = 0;
            timer = 0;
        }
    }

    public void executeRecipe() {
        if(level == null)
            return;

        totalTime = quickCheck.getRecipeFor(new RecipeWrapper(inventory), level).map((holder) -> holder.value().getProcessingDuration()).orElse(0) / (Math.max(size / 2, 1));
        timer = 0;
    }

    private void onFluidChanged(FluidStack stack) {
        if (level == null)
            return;
        if (!level.isClientSide) {
            setChanged();
            sendData();
        }
    }



    @Override
    public void tick() {
        super.tick();
        if(level == null)
            return;

        tickRecipe();

        CokeOvenBlockEntity controllerOven = getController();
        if (controllerOven != null) {
            if(level.isClientSide && controllerOven.totalTime != 0) {
                boolean timeCheck = controllerOven.timer < controllerOven.totalTime && controllerOven.timer > (controllerOven.totalTime * 0.95);
                doorAngle.chase(timeCheck || forceOpen ? 90 : 0, 0.1f, LerpedFloat.Chaser.EXP);
                doorAngle.tickChaser();
                if(!forceOpen)
                    manageDoors(timeCheck);
            }
        }

        if(createNextTick){
            createMultiblock();
            createNextTick = false;
        }
    }

    public void tickRecipe(){
        if(level == null)
            return;
        if(inventory.isEmpty() || totalTime == 0)
            return;

        RecipeHolder<CokingRecipe> recipeholder;
        if (!inventory.isEmpty()) {
            recipeholder = quickCheck.getRecipeFor(new RecipeWrapper(inventory), level).orElse(null);
        } else {
            recipeholder = null;
        }

        if(recipeholder == null) {
            totalTime = 0;
            timer = 0;
            return;
        }

        CokingRecipe recipe = recipeholder.value();

        if(timer == totalTime){
            totalTime = 0;
            timer = 0;
            inventory.getItem(0).shrink(recipe.getIngredients().getFirst().getItems()[0].getCount());

            Direction direction =  getBlockState().getValue(FACING);

            Vec3 dropVec = VecHelper.getCenterOf(worldPosition.relative(direction)).add(0,0.4,0);
            ItemEntity dropped = new ItemEntity(level, dropVec.x, dropVec.y, dropVec.z, recipe.getResultItem(level.registryAccess()).copy());
            dropped.setDefaultPickUpDelay();
            dropped.setDeltaMovement(direction.getAxis() == Direction.Axis.X ? direction == Direction.WEST ? -.01f : .01f : 0, 0.05f, direction.getAxis() == Direction.Axis.Z ? direction == Direction.NORTH ? -.01f : .01f : 0);
            level.addFreshEntity(dropped);

            if (!level.isClientSide) {
                setChanged();
                sendData();
            }
            onContentsChanged();
        }

        if(timer <= totalTime && primaryTank.getSpace() != 0 && secondaryTank.getSpace() != 0){
           primaryTank.fill(recipe.getPrimaryResult(), IFluidHandler.FluidAction.EXECUTE);
           secondaryTank.fill(recipe.getSecondaryResult(), IFluidHandler.FluidAction.EXECUTE);
           timer++;
        }
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        onContentsChanged();
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if(level == null)
            return false;

        TFMGTexts.header("coke_oven")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        CokeOvenBlockEntity controllerOven = getController();
        if(controllerOven != null) {
            double progress = ((double) controllerOven.timer / controllerOven.totalTime) * 100;
            if (controllerOven.totalTime == -1 || controllerOven.timer == 0)
                progress = 0;
            if (controllerOven.totalTime != -1)
                TFMGTexts.progress(TFMGTexts.percent(progress))
                        .style(ChatFormatting.GOLD)
                        .forGoggles(tooltip);
        }

        TFMGUtils.createStorageTooltip(this, tooltip);
        return true;
    }

    public void manageDoors(boolean open) {
        if(level == null)
            return;
        for(int i =0; i< size;i++){
            BlockPos pos = getBlockPos().above(i);
            if(level.getBlockEntity(pos) instanceof CokeOvenBlockEntity be && pos != getBlockPos()){
                be.forceOpen = open;
            }
        }
    }

    public boolean isController(){
        return controller == getBlockPos();
    }

    public void createMultiblock(){
        if(level == null)
            return;
        int maxSize = TFMGConfigs.common().machines.cokeOvenMaxSize.get();
        Direction facing = getBlockState().getValue(FACING);
        if(level.getBlockState(getBlockPos().relative(facing)).is(TFMGBlocks.COKE_OVEN.get())||level.getBlockState(getBlockPos().below()).is(TFMGBlocks.COKE_OVEN.get()))
            return;
        int size = 1;
            for(int i = 1;i<=maxSize;i++){
                boolean cantBuildMultiblock = false;
                for(BlockPos pos : BlockPos.betweenClosed(getBlockPos(),getBlockPos().above(i).relative(facing.getOpposite(),i))) {
                    if(!level.getBlockState(pos).is(TFMGBlocks.COKE_OVEN.get())){
                        cantBuildMultiblock = true;
                    } else if(level.getBlockState(pos).is(TFMGBlocks.COKE_OVEN.get()) &&level.getBlockState(pos).getValue(FACING) != facing){
                        cantBuildMultiblock = true;
                    }
                }
                if(cantBuildMultiblock)
                    break;
                size++;
            }
        for(BlockPos pos : BlockPos.betweenClosed(getBlockPos(),getBlockPos().above(size-1).relative(facing.getOpposite(),size-1))) {
            if(level.getBlockEntity(pos) instanceof CokeOvenBlockEntity be&&(!level.getBlockState(getBlockPos().relative(facing)).is(TFMGBlocks.COKE_OVEN.get())&&!level.getBlockState(getBlockPos().below()).is(TFMGBlocks.COKE_OVEN.get()))){

                be.controller = getBlockPos();
                be.refreshCapability();
            }
        }
        if(!level.getBlockState(getBlockPos().relative(facing)).is(TFMGBlocks.COKE_OVEN.get())&&!level.getBlockState(getBlockPos().below()).is(TFMGBlocks.COKE_OVEN.get()))
            setBlockStates(size);
        for(BlockPos pos : BlockPos.betweenClosed(getBlockPos(), getBlockPos().above(this.size-1).relative(facing.getOpposite(),this.size-1))){
            if(level.getBlockEntity(pos) instanceof CokeOvenBlockEntity be){
                if(Math.abs(getBlockPos().getX()-be.getBlockPos().getX())>=size || Math.abs(getBlockPos().getY()-be.getBlockPos().getY())>=size || Math.abs(getBlockPos().getZ()-be.getBlockPos().getZ())>=size)
                    if (be.controller == getBlockPos()||be.controller!=be.getBlockPos()) {
                        be.controller = be.getBlockPos();
                        be.refreshCapability();
                        be.forceOpen = false;
                        be.doorAngle.setValue(0);
                        level.setBlock(be.getBlockPos(), getBlockState().setValue(CokeOvenBlock.CONTROLLER_TYPE ,CokeOvenBlock.ControllerType.CASUAL), 2);
                    }
            }
        }
        this.size = size;
    }

    public void setBlockStates(int size) {
        if(level == null)
            return;
        if(size > 1) {
            level.setBlock(getBlockPos(), getBlockState().setValue(CokeOvenBlock.CONTROLLER_TYPE ,CokeOvenBlock.ControllerType.BOTTOM_ON), 2);
            level.setBlock(getBlockPos().above(size-1), getBlockState().setValue(CokeOvenBlock.CONTROLLER_TYPE ,CokeOvenBlock.ControllerType.TOP_ON), 2);
        } else level.setBlock(getBlockPos(), getBlockState().setValue(CokeOvenBlock.CONTROLLER_TYPE ,CokeOvenBlock.ControllerType.CASUAL), 2);

        for(int i = 0; i < size; i++) {
            BlockPos pos = getBlockPos().above(i);
            if (i > 0 && i != size - 1) {
                level.setBlock(pos, getBlockState().setValue(CokeOvenBlock.CONTROLLER_TYPE, CokeOvenBlock.ControllerType.MIDDLE_ON), 2);
            }
        }
    }
    public void onPlaced(){
        createNextTick = true;
        updateOvenBlocks();
        if (level instanceof ServerLevel serverLevel)
           CatnipServices.NETWORK.sendToClientsTrackingChunk(serverLevel, new ChunkPos(getBlockPos()),new CokeOvenPacket(getBlockPos()));
    }

    @Override
    public void remove() {
        super.remove();
        updateOvenBlocks();
    }

    @Override
    public void destroy() {
        super.destroy();
        if(isController())
            ItemHelper.dropContents(level, worldPosition, inventory);
    }

    public void updateOvenBlocks(){
        if (level == null) return;
        int maxSize = TFMGConfigs.common().machines.cokeOvenMaxSize.get();
        Direction facing = getBlockState().getValue(FACING);
        for(BlockPos pos : BlockPos.betweenClosed(getBlockPos(), getBlockPos().below(maxSize).relative(facing.getOpposite(), maxSize))){
            if(level.getBlockEntity(pos) instanceof CokeOvenBlockEntity be){
                be.createMultiblock();
            }
        }
    }

    public CokeOvenBlockEntity getController() {
        if(level == null)
            return null;
        CokeOvenBlockEntity cokeOven;
        if(level.getBlockEntity(controller) instanceof CokeOvenBlockEntity controllerOven){
            cokeOven = controllerOven;
        } else {
            controller = getBlockPos();
            cokeOven = this;
        }
        return cokeOven;
    }

    private void refreshCapability() {
        if(level == null)
            return;
        primaryFluidCapability = getController().primaryTank;
        secondaryFluidCapability = getController().secondaryTank;
        itemCapability = getController().inventory;
        invalidateCapabilities();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                TFMGBlockEntities.COKE_OVEN.get(),
                (be, context) -> context == Direction.UP ? be.secondaryFluidCapability : be.primaryFluidCapability
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                TFMGBlockEntities.COKE_OVEN.get(),
                (be, context) -> be.itemCapability
        );
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound,registries , clientPacket);
        compound.putInt("Timer", timer);
        compound.putInt("TotalTime", totalTime);
        compound.put("Inventory", inventory.serializeNBT(registries));
        compound.put("PrimaryTankContent", primaryTank.writeToNBT(registries,new CompoundTag()));
        compound.put("SecondaryTankContent", secondaryTank.writeToNBT(registries,new CompoundTag()));
        compound.putLong("Controller", controller.asLong());
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound,registries , clientPacket);
        timer = compound.getInt("Timer");
        totalTime = compound.getInt("TotalTime");
        inventory.deserializeNBT(registries,compound.getCompound("Inventory"));
        primaryTank.readFromNBT(registries,compound.getCompound("PrimaryTankContent"));
        secondaryTank.readFromNBT(registries,compound.getCompound("SecondaryTankContent"));
        controller = BlockPos.of(compound.getLong("Controller"));
    }
}
