package com.drmangotea.tfmg.content.machinery.metallurgy.blast_furnace;

import com.drmangotea.tfmg.base.TFMGUtils;
import com.drmangotea.tfmg.base.lang.TFMGTexts;
import com.drmangotea.tfmg.config.TFMGConfigs;
import com.drmangotea.tfmg.datagen.TFMGDamageSources;
import com.drmangotea.tfmg.recipes.CokingRecipe;
import com.drmangotea.tfmg.recipes.IndustrialBlastingRecipe;
import com.drmangotea.tfmg.recipes.input.IndustrialBlastingRecipeInput;
import com.drmangotea.tfmg.registry.TFMGBlockEntities;
import com.drmangotea.tfmg.registry.TFMGFluids;
import com.drmangotea.tfmg.registry.TFMGRecipeTypes;
import com.drmangotea.tfmg.registry.TFMGTags;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.fluid.CombinedTankWrapper;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.item.SmartInventory;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Clearable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;

public class BlastFurnaceOutputBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation, Clearable {

    public SmartInventory inputInventory;
    public SmartInventory fluxInventory;
    public FluidTank primaryTank;
    public FluidTank secondaryTank;
    protected IFluidHandler fluidCapability;
    public IItemHandlerModifiable itemCapability;
    public int fuel = 0;
    public int fuelConsumeTimer = 0;
    public float duration;
    public int timer = -1;
    public static final int STORAGE_SPACE = 64;
    public LerpedFloat coalCokeHeight = LerpedFloat.linear();
    public final BlastFurnaceMultiblock multiblock;

    private final RecipeManager.CachedCheck<IndustrialBlastingRecipeInput, IndustrialBlastingRecipe> quickCheck;


    public BlastFurnaceOutputBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setLazyTickRate(10);
        multiblock = new BlastFurnaceMultiblock(this);
        inputInventory = new SmartInventory(1, this)
                .forbidInsertion()
                .forbidExtraction()
                .withMaxStackSize(64);
        fluxInventory = new SmartInventory(1, this)
                .forbidInsertion()
                .forbidExtraction()
                .withMaxStackSize(64).whenContentsChanged(i -> this.onContentsChanged());

        primaryTank = new SmartFluidTank(4000, this::onFluidChanged);

        secondaryTank = new SmartFluidTank(4000, this::onFluidChanged);


        itemCapability = new CombinedInvWrapper(inputInventory, fluxInventory);
        fluidCapability = new CombinedTankWrapper(primaryTank, secondaryTank);

        this.quickCheck = RecipeManager.createCheck(TFMGRecipeTypes.INDUSTRIAL_BLASTING.getType());
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                TFMGBlockEntities.BLAST_FURNACE_OUTPUT.get(),
                (be, context) -> be.fluidCapability
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                TFMGBlockEntities.BLAST_FURNACE_OUTPUT.get(),
                (be, context) -> be.itemCapability
        );
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    private void onFluidChanged(FluidStack stack) {
        if (level == null)
            return;
        if (!level.isClientSide) {
            setChanged();
            sendData();
        }
    }

    public void onContentsChanged() {
        if (!inputInventory.isEmpty() && timer == -1) {
            executeRecipe();
        }
        if(inputInventory.isEmpty()) {
            timer = -1;
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        TFMGTexts.BlastFurnace.stats(inputInventory.getStackInSlot(0).getCount()).forGoggles(tooltip, 1);
        TFMGTexts.BlastFurnace.height(multiblock.getSize()).forGoggles(tooltip, 1);
        TFMGTexts.BlastFurnace.fuelAmount(fuel).forGoggles(tooltip, 1);
        if (timer != -1)
            TFMGTexts.BlastFurnace.timer(timer).forGoggles(tooltip, 1);
        if (multiblock.isReinforced())
            TFMGTexts.BlastFurnace.reinforced().forGoggles(tooltip);

        TFMGUtils.createStorageTooltip(this, tooltip);
        return true;
    }

    public void executeRecipe() {
        if (level == null)
            return;

        RecipeHolder<IndustrialBlastingRecipe> recipeholder;
        if (!inputInventory.isEmpty()) {
            recipeholder = quickCheck.getRecipeFor(new IndustrialBlastingRecipeInput(inputInventory.getItem(0), fluxInventory.getItem(0)), level).orElse(null);
        } else {
            recipeholder = null;
        }

        if(recipeholder == null) {
            timer = -1;
            return;
        }

        IndustrialBlastingRecipe recipe = recipeholder.value();

        if (recipe.getIngredients().size() > 1)
            if (!(recipe.getIngredients().get(1).test(fluxInventory.getItem(0))))
                return;

        if (fluxInventory.getItem(0).getCount() < recipe.getIngredients().size() - 1)
            return;

        int baseDuration = recipe.getProcessingDuration() * 20;
        int height = multiblock.getSize();
        int maxHeight = TFMGConfigs.common().machines.blastFurnaceMaxHeight.get();
        double maxTimeModifier = TFMGConfigs.common().machines.blastFurnaceHeightSpeedModifier.get();
        double timeModifier = maxHeight / (((double) baseDuration / 2) * maxTimeModifier);

        timer = (int) (baseDuration - (height / timeModifier));
        if (multiblock.isReinforced())
            timer /= 2;
    }

    boolean extractedAir = false;

    @Override
    public void tick() {
        super.tick();

        if (level == null)
            return;

        if (level.isClientSide) {
            coalCokeHeight.chase(Math.min(fuel + inputInventory.getStackInSlot(0).getCount(), 24), 0.1f, LerpedFloat.Chaser.EXP);
            coalCokeHeight.tickChaser();
        }

        BlockPos behindPos = getBlockPos().above().relative(getBlockState().getValue(FACING).getOpposite());
        if (level.getBlockState(behindPos).is(TFMGTags.Blocks.BLAST_FURNACE_MELTS.tag)) {
            level.removeBlock(behindPos, false);
        }

        if (inputInventory.isEmpty())
            return;
        if (multiblock.getSize() < 3)
            return;

        if (fuelConsumeTimer >= TFMGConfigs.common().machines.blastFurnaceFuelConsumption.get() && fuel > 0) {
            fuelConsumeTimer = 0;
            fuel--;
        }

        RecipeHolder<IndustrialBlastingRecipe> recipeholder;
        if (!inputInventory.isEmpty()) {
            recipeholder = quickCheck.getRecipeFor(new IndustrialBlastingRecipeInput(inputInventory.getItem(0), fluxInventory.getItem(0)), level).orElse(null);
        } else {
            recipeholder = null;
        }

        if(recipeholder == null) {
            timer = -1;
            return;
        }

        IndustrialBlastingRecipe recipe = recipeholder.value();
        if (timer > -1) {
            if (timer == 0) {
                if (canProcess(recipe)) {
                    inputInventory.getItem(0).shrink(1);
                    if (recipe.getIngredients().size() > 1)
                        fluxInventory.getItem(0).shrink(recipe.getIngredients().size() - 1);
                    primaryTank.fill(recipe.getPrimaryResult(), IFluidHandler.FluidAction.EXECUTE);
                    if (recipe.getFluidResults().size() > 1)
                        secondaryTank.fill(recipe.getSecondaryResult(), IFluidHandler.FluidAction.EXECUTE);
                    timer = -1;
                    sendData();
                    setChanged();
                }
            }
            if (timer > 0 && fuel > 0) {
                BlastFurnaceHatchBlockEntity tuyere = multiblock.getTuyereBlockEntity();
                if (recipe.hotAirUsage > 0 && tuyere == null) {
                    return;
                }
                if (tuyere != null && !extractedAir) {
                    int simulated = tuyere.tank.drain(new FluidStack(FluidHelper.convertToStill(TFMGFluids.HOT_AIR.get()), recipe.hotAirUsage), IFluidHandler.FluidAction.SIMULATE).getAmount();
                    if (simulated < recipe.hotAirUsage)
                        return;
                    tuyere.tank.drain(new FluidStack(FluidHelper.convertToStill(TFMGFluids.HOT_AIR.get()), recipe.hotAirUsage), IFluidHandler.FluidAction.EXECUTE);
                    extractedAir = true;
                }
                if (!recipe.getGasByproduct().isEmpty()) {
                    if (level.getBlockEntity(getBlockPos().relative(getBlockState().getValue(FACING).getOpposite()).above(multiblock.getSize())) instanceof BlastFurnaceHatchBlockEntity topHatch) {
                        topHatch.tank.fill(recipe.getGasByproduct(), IFluidHandler.FluidAction.EXECUTE);
                    }
                }
                if (level.isClientSide())
                    makeParticles();
                hurtEntities();
                timer--;
                fuelConsumeTimer++;
                extractedAir = false;
                if (!level.isClientSide) {
                    setChanged();
                    sendData();
                }
            }
        }
    }

    public void makeParticles() {
        if (level == null)
            return;
        Random random = new Random();
        Direction direction = getBlockState().getValue(FACING).getOpposite();
        BlockPos pos = getBlockPos().above().relative(direction);
        int shouldSpawnSmoke = random.nextInt(7);
        if (shouldSpawnSmoke == 0) {
            level.addParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, pos.getX() + random.nextFloat(0.6f) + 0.2, pos.getY() + 1, pos.getZ() + random.nextFloat(0.6f) + 0.2, 0.0D, 0.08D, 0.0D);
        }
    }

    private boolean canProcess(IndustrialBlastingRecipe recipe) {
        if (fuel == 0)
            return false;
        if (!primaryTank.getFluid().isEmpty() && !primaryTank.getFluid().getFluid().isSame(recipe.getPrimaryResult().getFluid()))
            return false;
        if (!secondaryTank.getFluid().isEmpty() && !secondaryTank.getFluid().getFluid().isSame(recipe.getSecondaryResult().getFluid()))
            return false;
        if (!(primaryTank.getSpace() >= recipe.getPrimaryResult().getAmount()))
            return false;
        if (recipe.getFluidResults().size() > 1)
            if (!(secondaryTank.getSpace() >= recipe.getSecondaryResult().getAmount()))
                return false;
        return true;
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        this.multiblock.evaluate();
        onContentsChanged();
        collectItems();
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos()).setMaxY(getBlockPos().getY() + 2);
    }

    public void hurtEntities() {
        if (level == null)
            return;
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, new AABB(this.getBlockPos().relative(getBlockState().getValue(FACING).getOpposite()).above()));

        for (LivingEntity entity : entities) {
            if (!entity.fireImmune()) {
                entity.setRemainingFireTicks(15);
                if (entity.hurt(TFMGDamageSources.blastFurnace(level), 4.0F)) {
                    entity.playSound(SoundEvents.GENERIC_BURN, 0.4F, 2.0F + entity.getRandom().nextFloat() * 0.4F);
                }
            }
        }
    }

    public void collectItems() {
        if (level == null)
            return;
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, new AABB(this.getBlockPos().relative(getBlockState().getValue(FACING).getOpposite()).above()));
        if (items.isEmpty())
            return;

        ItemStack itemStack = items.getFirst().getItem();

        for (int i = 0; i < 64; i++) {
            if (itemStack.isEmpty())
                return;
            if (itemStack.is(TFMGTags.Items.BLAST_FURNACE_FUEL.tag) && fuel < STORAGE_SPACE) {
                fuel++;
                itemStack.shrink(1);
                continue;
            }
            if (itemStack.is(TFMGTags.Items.FLUX.tag) && fluxInventory.getItem(0).getCount() < itemStack.getMaxStackSize()) {
                if (fluxInventory.isEmpty() || fluxInventory.getItem(0).is(itemStack.getItem())) {
                    fluxInventory.setItem(0, new ItemStack(itemStack.getItem(), fluxInventory.getItem(0).getCount() + 1));
                    itemStack.shrink(1);
                    continue;
                }
            }
            if (inputInventory.getItem(0).getCount() < itemStack.getMaxStackSize()) {
                if (inputInventory.isEmpty() || inputInventory.getItem(0).is(itemStack.getItem())) {
                    inputInventory.setItem(0, new ItemStack(itemStack.getItem(), inputInventory.getItem(0).getCount() + 1));
                    itemStack.shrink(1);
                }
            }
        }
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound,registries , clientPacket);
        multiblock.read(compound, "MultiblockData", registries, clientPacket);
        inputInventory.deserializeNBT(registries,compound.getCompound("InputItems"));
        fluxInventory.deserializeNBT(registries,compound.getCompound("Flux"));
        timer = compound.getInt("Timer");
        fuel = compound.getInt("Fuel");
        fuelConsumeTimer = compound.getInt("FuelConsumeTimer");
        primaryTank.readFromNBT(registries,compound.getCompound("PrimaryTankContent"));
        secondaryTank.readFromNBT(registries,compound.getCompound("SecondaryTankContent"));
    }

    @Override
    public void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound,registries , clientPacket);
        compound.put("MultiblockData", multiblock.write(registries, clientPacket));
        compound.put("InputItems", inputInventory.serializeNBT(registries));
        compound.put("Flux", fluxInventory.serializeNBT(registries));
        compound.putInt("Timer", timer);
        compound.putInt("Fuel", fuel);
        compound.putInt("FuelConsumeTimer", fuelConsumeTimer);
        compound.put("PrimaryTankContent", primaryTank.writeToNBT(registries,new CompoundTag()));
        compound.put("SecondaryTankContent", secondaryTank.writeToNBT(registries,new CompoundTag()));
    }

    @Override
    public void destroy() {
        super.destroy();
        ItemHelper.dropContents(level, worldPosition, inputInventory);
        ItemHelper.dropContents(level, worldPosition, fluxInventory);
    }

    @Override
    public void clearContent() {
        this.inputInventory.clearContent();
        this.fluxInventory.clearContent();
    }
}
