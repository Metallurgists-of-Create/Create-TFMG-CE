package com.drmangotea.tfmg.content.machinery.misc.winding_machine;

import com.drmangotea.tfmg.TFMG;
import com.drmangotea.tfmg.TFMGRegistries;
import com.drmangotea.tfmg.base.lang.TFMGLang;
import com.drmangotea.tfmg.registry.TFMGDataComponents;
import com.drmangotea.tfmg.registry.TFMGItems;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Objects;

import static com.drmangotea.tfmg.base.blocks.WallMountBlock.FACING;

public class SpoolItem extends Item {

    public final int barColor;
    public final ResourceLocation cableTypeKey;

    public SpoolItem(Properties properties, int barColor, ResourceLocation cableTypeKey) {
        super(properties);
        this.barColor = barColor;
        this.cableTypeKey = cableTypeKey;
    }


    @Override
    public void onCraftedBy(ItemStack stack, Level p_41448_, Player p_41449_) {
        stack.set(TFMGDataComponents.SPOOL_AMOUNT, 1000);
        super.onCraftedBy(stack, p_41448_, p_41449_);

    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isCrouching() && stack.getOrDefault(TFMGDataComponents.POSITION, 0f).longValue() != 0f) {
            stack.set(TFMGDataComponents.POSITION, 0l);
            stack.remove(TFMGDataComponents.POSITION);
            stack.remove(TFMGDataComponents.X_POS);
            stack.remove(TFMGDataComponents.Y_POS);
            stack.remove(TFMGDataComponents.Z_POS);
            if (level.isClientSide)
                player.displayClientMessage(TFMGLang.translateDirect("wires.removed_data")
                        .withStyle(ChatFormatting.YELLOW), true);
            return InteractionResultHolder.success(stack);

        }

        return super.use(level, player, hand);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {

        String text = TFMGLang.translateDirect("tooltip.coils").getString();

        tooltip.add(TFMGLang.text(text + stack.getOrDefault(TFMGDataComponents.SPOOL_AMOUNT, 0)).component().withStyle(ChatFormatting.GREEN)

        );

        if (stack.get(TFMGDataComponents.POSITION) == null)
            return;
        BlockPos pos = BlockPos.of(stack.get(TFMGDataComponents.POSITION));
        if (pos.asLong() != 0)
            tooltip.add(TFMGLang.text(pos.getX() + " " + pos.getY() + " " + pos.getZ()).component()
                    .withStyle(ChatFormatting.AQUA)
            );
        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (level.getBlockEntity(pos) instanceof WindingMachineBlockEntity be) {
            ItemStack oldSpool = ItemStack.EMPTY;
            if (!be.spool.isEmpty()) {
                oldSpool = be.spool;
            }
            be.spool = context.getItemInHand();
            context.getPlayer().setItemInHand(context.getHand(), oldSpool);
            be.sendData();
            be.setChanged();
//
            return InteractionResult.SUCCESS;
        }

        if (stack.get(TFMGDataComponents.SPOOL_AMOUNT) == null)
            return InteractionResult.PASS;
        if (level.isClientSide)
            return InteractionResult.SUCCESS;

        if (Objects.equals(cableTypeKey, TFMG.asResource("empty")))
            return InteractionResult.PASS;
        Direction direction = level.getBlockState(pos).getValue(FACING);





//
//

        return InteractionResult.PASS;
    }


    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean p_41408_) {


        if (stack.get(TFMGDataComponents.SPOOL_AMOUNT) == null)
            return;

        if (stack.get(TFMGDataComponents.SPOOL_AMOUNT) == 0 && entity instanceof Player player && !stack.is(TFMGItems.EMPTY_SPOOL.get())) {
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

        if (stack.get(TFMGDataComponents.SPOOL_AMOUNT) == null)
            return 13;

        return (int) (13f * ((float) stack.get(TFMGDataComponents.SPOOL_AMOUNT) / 1000));
    }

}
