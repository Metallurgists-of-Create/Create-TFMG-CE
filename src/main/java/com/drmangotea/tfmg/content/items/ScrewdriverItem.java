package com.drmangotea.tfmg.content.items;

import com.drmangotea.tfmg.content.decoration.pipes.ILockablePipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;


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
}
