package com.drmangotea.tfmg.content.machinery.misc.winding_machine;

import com.drmangotea.tfmg.TFMG;
import com.drmangotea.tfmg.TFMGRegistries;
import com.drmangotea.tfmg.base.annotation.NothingNullByDefault;
import com.drmangotea.tfmg.base.lang.TFMGLang;
import com.drmangotea.tfmg.registry.TFMGDataComponents;
import com.drmangotea.tfmg.registry.TFMGItems;
import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Objects;

import static com.drmangotea.tfmg.base.blocks.WallMountBlock.FACING;

@NothingNullByDefault
public class SpoolItem extends Item {
    public final int barColor;
    public final ResourceLocation cableTypeKey;

    public SpoolItem(Properties properties, int barColor, ResourceLocation cableTypeKey) {
        super(properties);
        this.barColor = barColor;
        this.cableTypeKey = cableTypeKey;
    }

    public static boolean isDifferent(ItemStack using, ItemStack inserted) {
        if (using.is(inserted.getItem())) {
            return !using.getOrDefault(TFMGDataComponents.SPOOL_AMOUNT, 0).equals(inserted.getOrDefault(TFMGDataComponents.SPOOL_AMOUNT, 0));
        }
        return true;
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        stack.set(TFMGDataComponents.SPOOL_AMOUNT, 1000);
        super.onCraftedBy(stack, level, player);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        BlockPos fallback = new BlockPos(0, level.getMaxBuildHeight() + 1, 0); // Impossible block position, better than 0,0,0
        ItemStack stack = player.getItemInHand(hand);
        if (player.isCrouching() && stack.has(TFMGDataComponents.POSITION)) {
            BlockPos pos = stack.getOrDefault(TFMGDataComponents.POSITION, fallback);
            if (pos == fallback)
                return InteractionResultHolder.pass(stack);
            stack.remove(TFMGDataComponents.POSITION);
            if (level.isClientSide)
                player.displayClientMessage(
					TFMGLang.translateDirect("wires.removed_data").withStyle(ChatFormatting.YELLOW),
					true
				);
            return InteractionResultHolder.success(stack);
        }
        return super.use(level, player, hand);
    }

    @Override @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        String text = TFMGLang.translateDirect("tooltip.coils").getString();
        tooltip.add(TFMGLang.text(text + stack.getOrDefault(TFMGDataComponents.SPOOL_AMOUNT, 0)).component().withStyle(ChatFormatting.GREEN));

        if (stack.get(TFMGDataComponents.POSITION) == null)
            return;
        BlockPos pos = stack.get(TFMGDataComponents.POSITION);
        if (pos != null)
            tooltip.add(TFMGLang.text(pos.getX() + " " + pos.getY() + " " + pos.getZ()).component().withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        BlockPos fallback = new BlockPos(0, level.getMaxBuildHeight() + 1, 0); // Impossible block position, better than 0,0,0
        BlockPos posToConnect = stack.getOrDefault(TFMGDataComponents.POSITION, fallback);

        BlockState state = level.getBlockState(pos);

        Direction direction = state.hasProperty(FACING) ? state.getValue(FACING) : null;

        if (player == null)
            return InteractionResult.PASS;

        if (level.getBlockEntity(pos) instanceof WindingMachineBlockEntity be) {
            if (player instanceof DeployerFakePlayer && stack.is(TFMGItems.EMPTY_SPOOL))
                return InteractionResult.PASS;

            ItemStack oldSpool = ItemStack.EMPTY;
            if (!be.getSpool().isEmpty()) {
                oldSpool = be.getSpool();
            }
            be.setSpool(stack);
            player.setItemInHand(context.getHand(), oldSpool);
            be.sendData();
            be.setChanged();
            return InteractionResult.SUCCESS;
        }
        if (!stack.has(TFMGDataComponents.SPOOL_AMOUNT))
            return InteractionResult.PASS;
        if (level.isClientSide)
            return InteractionResult.SUCCESS;

        if (Objects.equals(cableTypeKey, TFMG.asResource("empty")))
            return InteractionResult.PASS;

        return InteractionResult.PASS;
    }


    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
		var amount = stack.get(TFMGDataComponents.SPOOL_AMOUNT);
        if (amount == null)
            return;

        if (amount == 0 && entity instanceof Player player && !stack.is(TFMGItems.EMPTY_SPOOL.get())) {
            player.getInventory().setItem(slot, TFMGItems.EMPTY_SPOOL.asStack());
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return !Objects.equals(cableTypeKey, TFMG.asResource("empty")) && TFMGRegistries.CABLE_TYPE_REGISTRY.containsKey(cableTypeKey);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return barColor;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return (int) (13f * ((float) stack.getOrDefault(TFMGDataComponents.SPOOL_AMOUNT, 1000) / 1000));
    }
}
