package com.drmangotea.tfmg.base;


import com.drmangotea.tfmg.TFMGRegistries;
import com.drmangotea.tfmg.base.lang.TFMGLang;
import com.drmangotea.tfmg.base.spark.ElectricSparkParticle;
import com.drmangotea.tfmg.base.spark.Spark;
import com.drmangotea.tfmg.content.electricity.connection.cable_type.CableType;
import com.drmangotea.tfmg.content.machinery.vat.electrode_holder.electrode.Electrode;
import com.drmangotea.tfmg.registry.TFMGEntityTypes;
import com.simibubi.create.Create;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.lang.LangBuilder;
import net.createmod.catnip.outliner.Outliner;
import net.createmod.catnip.theme.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TFMGUtils {

    public static float toYRot(Direction facing) {
        return switch (facing){
            case DOWN -> 0.0F;
            case UP -> 0.0F;
            case NORTH -> 0.0F;
            case SOUTH -> 180F;
            case WEST -> 90;
            case EAST -> 270F;
        };
    }
    public static void createFireExplosion(Level level, Entity entity, BlockPos pos, int sparkAmount, float radius) {

        if (level.isClientSide && entity != null) level.broadcastEntityEvent(entity, (byte) 3);

        for (int i = 0; i < sparkAmount; i++) {
            float x = Create.RANDOM.nextFloat(360);
            float y = Create.RANDOM.nextFloat(360);
            float z = Create.RANDOM.nextFloat(360);
            Spark spark = TFMGEntityTypes.SPARK.create(level);
            spark.moveTo(pos.getX(), pos.getY() + 1, pos.getZ());

            float f = -Mth.sin(y * ((float) Math.PI / 180F)) * Mth.cos(x * ((float) Math.PI / 180F));
            float f1 = -Mth.sin((x + z) * ((float) Math.PI / 180F));
            float f2 = Mth.cos(y * ((float) Math.PI / 180F)) * Mth.cos(x * ((float) Math.PI / 180F));
            spark.shoot(f, f1, f2, 0.3f, 1);
            level.addFreshEntity(spark);
        }
        level.explode(null, pos.getX(), pos.getY(), pos.getZ(), radius, Level.ExplosionInteraction.BLOCK);
    }
    public static void playSound(Level level, BlockPos pos, SoundEvent sound, SoundSource source){
        playSound(level,pos,sound,source,1,1,null);
    }
    public static void playSound(Level level, BlockPos pos, SoundEvent sound, SoundSource source, Player player){
        playSound(level,pos,sound,source,1,1,player);
    }
    public static void playSound(Level level, BlockPos pos, SoundEvent sound, SoundSource source, float volume, float pitch){
        playSound(level,pos,sound,source,volume,pitch,null);
    }
    public static void playSound(Level level, BlockPos pos, SoundEvent sound, SoundSource source, float volume, float pitch, Player player){
        level.playSound(player,pos,sound,source,volume,pitch);
    }

    public static void blowUpTank(FluidTankBlockEntity tank, int power) {

        if (tank == null || tank.getControllerBE() == null) return;
        FluidTankBlockEntity be = tank.getControllerBE();

        for (int xOffset = 0; xOffset < be.getWidth(); xOffset++) {
            for (int zOffset = 0; zOffset < be.getWidth(); zOffset++) {
                for (int yOffset = 0; yOffset < be.getHeight(); yOffset++) {

                    BlockPos pos = be.getBlockPos().offset(xOffset, yOffset, zOffset);

                    be.getLevel().destroyBlock(pos, false);
                }
            }
        }

        createFireExplosion(be.getLevel(), null, new BlockPos(be.getBlockPos().getX() + (be.getWidth() / 2), be.getBlockPos().getY() + (be.getHeight() / 2), be.getBlockPos().getZ() + (be.getWidth() / 2)), power * 15, (float) power);
    }

    public static void createOutline(Vec3 pos1, Vec3 pos2,String name,Color color){
        createOutline(pos1,pos2,name,color,1/32f);
    }

    public static void createOutline(Vec3 pos1, Vec3 pos2,String name,Color color,float width){
        Outliner.getInstance().showAABB(name, new AABB(pos1, pos2))
                .lineWidth(width)
                .colored(color);
    }

    public static String fromId(String key) {
        String s = key.replaceAll("_", " ");
        s = Arrays.stream(StringUtils.splitByCharacterTypeCamelCase(s)).map(StringUtils::capitalize).collect(Collectors.joining(" "));
        s = StringUtils.normalizeSpace(s);
        return s;
    }

    public static String toHumanReadable(String key) {
        String s = key.replaceAll("_", " ");
        s = Arrays.stream(StringUtils.splitByCharacterTypeCamelCase(s)).map(StringUtils::capitalize).collect(Collectors.joining(" "));
        s = StringUtils.normalizeSpace(s);
        return s;
    }

    public static void spawnElectricParticles(Level level, BlockPos pos) {
        if (level == null) return;
        RandomSource r = level.getRandom();

        for (int i = 0; i < r.nextInt(40); i++) {
            float x = Create.RANDOM.nextFloat(2) - 1;
            float y = Create.RANDOM.nextFloat(2) - 1;
            float z = Create.RANDOM.nextFloat(2) - 1;

            level.addParticle(new ElectricSparkParticle.Data(), pos.getX() + 0.5f + x, pos.getY() + 0.5f + y, pos.getZ() + 0.5f + z, x, y, z);
        }
    }

    public static float getDistance(BlockPos pos1, BlockPos pos2, boolean _2D) {
        float x = Math.abs(pos1.getX() - pos2.getX());
        float y = Math.abs(pos1.getY() - pos2.getY());
        float z = Math.abs(pos1.getZ() - pos2.getZ());

        return (float) Math.sqrt(x * x + z * z + (_2D?0:y*y));
    }

    public static void createStorageTooltip(BlockEntity be, List<Component> tooltip) {
        createFluidTooltip(be, tooltip);
        createItemTooltip(be, tooltip);
    }
    /// makes a goggle tooltip for every tank a block entity has
    public static boolean createFluidTooltip(BlockEntity be, List<Component> tooltip) {
        LangBuilder mb = CreateLang.translate("generic.unit.millibuckets");

        /////////
        IFluidHandler handler = Capabilities.FluidHandler.BLOCK.getCapability(be.getLevel(),be.getBlockPos(),be.getBlockState(),be,null);


        if(handler == null)
            return true;

        IFluidHandler tank = handler;
        if (tank.getTanks() == 0) return false;

        TFMGLang.translate("goggles.fluid_storage").style(ChatFormatting.GRAY).forGoggles(tooltip);


        boolean isEmpty = true;
        for (int i = 0; i < tank.getTanks(); i++) {
            FluidStack fluidStack = tank.getFluidInTank(i);
            //if (fluidStack.isEmpty()) continue;
            CreateLang.fluidName(fluidStack).style(ChatFormatting.GRAY).forGoggles(tooltip, 1);
            CreateLang.builder().add(CreateLang.number(fluidStack.getAmount()).add(mb).style(ChatFormatting.DARK_GREEN)).text(ChatFormatting.GRAY, " / ").add(CreateLang.number(tank.getTankCapacity(i)).add(mb).style(ChatFormatting.DARK_GRAY)).forGoggles(tooltip, 1);
            isEmpty = false;
        }
        if (tank.getTanks() > 1) {
            if (isEmpty) tooltip.remove(tooltip.size() - 1);
            return true;
        }
        if (!isEmpty) return true;

        CreateLang.translate("goggles.fluid_storage").add(CreateLang.number(tank.getTankCapacity(0)).add(mb).style(ChatFormatting.DARK_GREEN)).style(ChatFormatting.DARK_GRAY).forGoggles(tooltip, 1);
        return true;
    }


    public static boolean createItemTooltip(BlockEntity be, List<Component> tooltip) {

        IItemHandlerModifiable handler = (IItemHandlerModifiable) Capabilities.ItemHandler.BLOCK.getCapability(be.getLevel(),be.getBlockPos(),be.getBlockState(),be,null);

        IItemHandlerModifiable inventory = handler;
        if (inventory.getSlots() == 0) return false;
        CreateLang.translate("goggles.item_storage").style(ChatFormatting.GRAY).forGoggles(tooltip);
        boolean isEmpty = true;
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack itemStack = inventory.getStackInSlot(i);

            if (itemStack.isEmpty()) continue;
            CreateLang.itemName(itemStack).style(ChatFormatting.DARK_GREEN).add(Component.literal(" x " + itemStack.getCount())).style(ChatFormatting.DARK_GREEN).forGoggles(tooltip, 1);
            isEmpty = false;
        }
        if (inventory.getSlots() > 1) {
            if (isEmpty) tooltip.remove(tooltip.size() - 1);
            return true;
        }
        if (!isEmpty) return true;

        CreateLang.translate("item_attributes.shulker_level.empty").style(ChatFormatting.DARK_GRAY).forGoggles(tooltip, 1);
        return true;
    }

    public static String formatUnits(double n, String unit) {
        if (n == 0)
            return Math.round(n) + unit;
        double var10000;
        if (n >= 1000000000) {
            var10000 = (double) Math.round((double) n / 1.0E8);
            return var10000 / 10.0 + "G" + unit;
        } else if (n >= 1000000) {
            var10000 = (double) Math.round((double) n / 100000.0);
            return var10000 / 10.0 + "M" + unit;
        } else if (n >= 1000) {
            var10000 = (double) Math.round((double) n / 100.0);
            return var10000 / 10.0 + "k" + unit;
        }
        // else if (n < 0.001) {
        //     var10000 = (double) Math.round((double) n * 10000000.0);
        //     return var10000 / 10.0 + "μ" + unit;
        // }
        else if (n < 1) {
            var10000 = (double) Math.round((double) n * 10000.0);
            return var10000 / 10.0 + "m" + unit;
        } else {
            return Math.round(n) + unit;
        }
    }

    public static void drainFilteredTank(SmartFluidTank tank, int amount) {
        tank.setFluid(new FluidStack(tank.getFluid().getFluidHolder(), Math.max(tank.getFluidAmount() - amount, 0)));
    }

    public static void fillFilteredTank(SmartFluidTank tank, FluidStack resource) {
        if (tank.getFluid().getFluid().isSame(resource.getFluid()) || tank.isEmpty())
            tank.setFluid(new FluidStack(resource.getFluid(), Math.min(tank.getFluidAmount() + resource.getAmount(), tank.getCapacity())));
    }

    public static Iterable<BlockPos> AABBtoBlockPos(AABB aabb) {
        return BlockPos.betweenClosed(new BlockPos((int) aabb.minX, (int) aabb.minY, (int) aabb.minZ), new BlockPos((int) aabb.maxX, (int) aabb.maxY, (int) aabb.maxZ));
    }

    public static SmartFluidTank createTank(int capacity, boolean extractionAllowed, Consumer<FluidStack> updateCallback) {
        return createTank(capacity, extractionAllowed, true, updateCallback, null);
    }

    public static SmartFluidTank createTank(int capacity, boolean extractionAllowed, boolean insertionAllowed, Consumer<FluidStack> updateCallback) {
        return createTank(capacity, extractionAllowed, insertionAllowed, updateCallback, null);
    }

    public static SmartFluidTank createTank(int capacity, boolean extractionAllowed, boolean insertionAllowed, Consumer<FluidStack> updateCallback, Fluid validFluid) {
        return new SmartFluidTank(capacity, updateCallback) {
            @Override
            public boolean isFluidValid(FluidStack stack) {

                if (validFluid == null) return true;

                return stack.getFluid().isSame(validFluid);
            }

            @Override
            public FluidStack drain(FluidStack resource, FluidAction action) {
                if (!extractionAllowed) return FluidStack.EMPTY;
                return super.drain(resource, action);
            }

            public FluidStack forceDrain(FluidStack resource, FluidAction action){
                return super.drain(resource,action);
            }

            @Override
            public FluidStack drain(int maxDrain, FluidAction action) {
                if (!extractionAllowed) return FluidStack.EMPTY;
                return super.drain(maxDrain, action);
            }

            @Override
            public int fill(FluidStack resource, FluidAction action) {
                if (!insertionAllowed) return 0;
                return super.fill(resource, action);
            }
        };
    }

    /// //////////////////////
    public static Electrode getElectrode(ResourceLocation key) {
        return TFMGRegistries.ELECTRODE_REGISTRY.get(key);
    }

    public static CableType getCableType(ResourceLocation key) {
        return TFMGRegistries.CABLE_TYPE_REGISTRY.get(key);
    }
}