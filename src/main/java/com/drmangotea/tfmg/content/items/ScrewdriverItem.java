package com.drmangotea.tfmg.content.items;

import com.drmangotea.tfmg.content.decoration.pipes.ILockablePipe;
import com.simibubi.create.foundation.utility.RaycastHelper;
import net.createmod.catnip.outliner.Outliner;
import net.createmod.ponder.api.PonderPalette;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;


public class ScrewdriverItem extends Item {
    public ScrewdriverItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Player player = pContext.getPlayer();
        BlockPos positionClicked = pContext.getClickedPos();
        Level level = pContext.getLevel();

        if (level.getBlockEntity(positionClicked) != null && player != null) {
            if (level.getBlockEntity(positionClicked) instanceof ILockablePipe lockablePipe) {
                lockablePipe.toggleLock(player, level, positionClicked, level.getBlockState(positionClicked));
                pContext.getItemInHand().hurtAndBreak(1, player, LivingEntity.getSlotForHand(pContext.getHand()));
                return InteractionResult.SUCCESS;
            }
        }
        return super.useOn(pContext);
    }

    private static AABB lastShownAABB = null;
    private static boolean render = false;
    private static int colour = PonderPalette.BLUE.getColor();

    @OnlyIn(Dist.CLIENT)
    public static void clientTick() {
        Player player = Minecraft.getInstance().player;
        if (player == null)
            return;
        ItemStack heldItemMainhand = player.getMainHandItem();
        ItemStack heldItemOffhand = player.getOffhandItem();
        if (!(heldItemMainhand.getItem() instanceof ScrewdriverItem) && !(heldItemOffhand.getItem() instanceof ScrewdriverItem))
            return;

        Level world = Minecraft.getInstance().level;

        if (world == null)
            return;

        BlockHitResult block = RaycastHelper.rayTraceRange(world, player, player.blockInteractionRange());
        BlockPos targetedPos = block.getBlockPos();

        BlockState state = world.getBlockState(targetedPos);
        VoxelShape shape = state.getShape(world, targetedPos);

        BlockEntity blockEntity = world.getBlockEntity(targetedPos);

        if (blockEntity instanceof ILockablePipe lockable) {
            lastShownAABB = shape.isEmpty() ? new AABB(BlockPos.ZERO) : shape.bounds().move(targetedPos);
            colour = lockable.locked() ? PonderPalette.RED.getColor() : PonderPalette.GREEN.getColor();
            render = true;
        } else {
            render = false;
        }

        if (render) {
            Outliner.getInstance().showAABB("lockable_pipe", lastShownAABB)
                    .colored(colour)
                    .lineWidth(1 / 32f);
        }
    }
}
