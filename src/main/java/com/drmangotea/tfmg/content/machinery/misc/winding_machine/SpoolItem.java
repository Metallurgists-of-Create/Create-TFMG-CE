package com.drmangotea.tfmg.content.machinery.misc.winding_machine;

import com.drmangotea.tfmg.TFMG;
import com.drmangotea.tfmg.TFMGRegistries;
import com.drmangotea.tfmg.base.TFMGUtils;
import com.drmangotea.tfmg.base.lang.TFMGLang;
import com.drmangotea.tfmg.content.electricity.connection.cable_type.CableType;
import com.drmangotea.tfmg.content.electricity.connection.cables.CableConnection;
import com.drmangotea.tfmg.content.electricity.connection.cables.CableConnectorBlockEntity;
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
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
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

    @Override @ParametersAreNonnullByDefault
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        stack.set(TFMGDataComponents.SPOOL_AMOUNT, 1000);
        super.onCraftedBy(stack, level, player);
    }

    @Override @NotNull @ParametersAreNonnullByDefault
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        BlockPos fallback = new BlockPos(0, level.getMaxBuildHeight() + 1, 0); // Impossible block position, better than 0,0,0
        ItemStack stack = player.getItemInHand(hand);
        if (player.isCrouching() && stack.has(TFMGDataComponents.POSITION)) {
            BlockPos pos = stack.getOrDefault(TFMGDataComponents.POSITION, fallback);
            if (pos == fallback)
                return InteractionResultHolder.pass(stack);
            if (level.getBlockEntity(pos) instanceof CableConnectorBlockEntity be)
                be.player = null;
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

    @Override @ParametersAreNonnullByDefault
    @OnlyIn(Dist.CLIENT)
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

    @Override @NotNull
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
            ItemStack oldSpool = ItemStack.EMPTY;
            if (!be.spool.isEmpty()) {
                oldSpool = be.spool;
            }
            be.spool = context.getItemInHand();
            player.setItemInHand(context.getHand(), oldSpool);
            be.sendData();
            be.setChanged();
            return InteractionResult.SUCCESS;
        }
        if (!stack.has(TFMGDataComponents.SPOOL_AMOUNT))
            return InteractionResult.PASS;
        if (!level.isClientSide) {
            if (Objects.equals(cableTypeKey, TFMG.asResource("empty")))
                return InteractionResult.PASS;
            for (int i = 0; i < 64; i++) {
                if (direction != null && level.getBlockEntity(pos.relative(direction)) instanceof CableConnectorBlockEntity) {
                    pos = pos.relative(direction);
                } else break;
            }
        }

        if (level.getBlockEntity(pos) instanceof CableConnectorBlockEntity be) {
            if (stack.has(TFMGDataComponents.POSITION)) {
                if (posToConnect.equals(pos)) {
                    stack.remove(TFMGDataComponents.POSITION);
                    if (level.isClientSide)
                        player.displayClientMessage(TFMGLang.translateDirect("wires.cant_connect_itself")
                                .withStyle(ChatFormatting.YELLOW), true);
                    be.player = null;
                    be.sendData();
                    be.setChanged();
                    return InteractionResult.SUCCESS;
                }
                for (int i = 0; i < 64; i++) {
                    if (direction != null && level.getBlockEntity(posToConnect.relative(direction)) instanceof CableConnectorBlockEntity) {
                        posToConnect = posToConnect.relative(direction);
                    } else break;
                }
                if (level.getBlockEntity(posToConnect) instanceof CableConnectorBlockEntity otherBE) {
                    CableType cableType = TFMGUtils.getCableType(cableTypeKey);

                    CableConnection connection1 = new CableConnection(otherBE.getBlockPos(),be.getBlockPos(), cableType, true);
                    CableConnection connection2 = new CableConnection(be.getBlockPos(),otherBE.getBlockPos(), cableType, false);

                    double distance = otherBE.getBlockPos().distManhattan(be.getBlockPos());
                    int turnsLeft = stack.getOrDefault(TFMGDataComponents.SPOOL_AMOUNT, 0);
					int amount = Math.round((float)(distance / 4)*80);
                    if (turnsLeft < amount) { return InteractionResult.PASS; }
                    if (be.connections.contains(connection1) || otherBE.connections.contains(connection2)) {
						if (level.isClientSide)
                            player.displayClientMessage(TFMGLang.translateDirect("wires.connection_already_created")
                                    .withStyle(ChatFormatting.YELLOW), true);
                        be.player = null;
                        be.sendData();
                        be.setChanged();
                        return InteractionResult.SUCCESS;
                    }
                    be.connections.add(connection1);
                    otherBE.connections.add(connection2);

                    stack.set(TFMGDataComponents.SPOOL_AMOUNT, turnsLeft - amount);
                    be.player = null;
                    otherBE.player = null;
                    be.setChanged();
                    otherBE.setChanged();
                    be.sendData();
                    otherBE.sendData();
                    stack.remove(TFMGDataComponents.POSITION);
                }
                //
                be.player = null;
                if (!level.isClientSide()) {
                    be.data.connectNextTick = true;
                }
            } else {
                stack.set(TFMGDataComponents.POSITION, be.getBlockPos());
                be.player = player;
                be.color = barColor;
                be.sendData();
                be.setChanged();
                if (!level.isClientSide())
                    be.onPlaced();
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }


    @Override @ParametersAreNonnullByDefault
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
        if (stack.get(TFMGDataComponents.SPOOL_AMOUNT) == null)
            return;

        if (stack.get(TFMGDataComponents.SPOOL_AMOUNT) == 0 && entity instanceof Player player && !stack.is(TFMGItems.EMPTY_SPOOL.get())) {
            player.getInventory().setItem(slot, TFMGItems.EMPTY_SPOOL.asStack());
        }
    }

    @Override @ParametersAreNonnullByDefault
    public boolean isBarVisible(ItemStack stack) {
        return !Objects.equals(cableTypeKey, TFMG.asResource("empty")) && TFMGRegistries.CABLE_TYPE_REGISTRY.containsKey(cableTypeKey);
    }

    @Override @ParametersAreNonnullByDefault
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