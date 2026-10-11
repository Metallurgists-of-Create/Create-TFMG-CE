package com.drmangotea.tfmg.base.debug;

import com.drmangotea.tfmg.TFMG;
import com.drmangotea.tfmg.base.annotation.NothingNullByDefault;
import com.drmangotea.tfmg.content.decoration.tanks.steel.SteelTankBlock;
import com.drmangotea.tfmg.content.decoration.tanks.steel.SteelTankBlockEntity;
import com.drmangotea.tfmg.content.electricity.base.IElectric;
import com.drmangotea.tfmg.content.electricity.connection.cables.CableConnectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

@NothingNullByDefault
public class DebugCinderBlockItem extends Item {
    public DebugCinderBlockItem(Properties p) {
        super(p);
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return true;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
		Player player = context.getPlayer();
		
		if (level.getBlockEntity(pos) instanceof CableConnectorBlockEntity be) {
			TFMG.LOGGER.info("{}:\n {}", pos, be.connections);
		}

        if (player != null && level.getBlockEntity(pos) instanceof IElectric be) {
			if(player.isCrouching()){
				be.recalculateNetworkResistance();
			} else {
				be.updateNextTick();
				TFMG.LOGGER.debug("Network at {} with size {}", BlockPos.of(be.getData().electricalNetworkId), be.getOrCreateElectricNetwork().members.size());
			}
        }

        if (level.getBlockEntity(pos) instanceof SteelTankBlockEntity be) {
            if (player != null && player.isCrouching() && be.getLevel() instanceof Level pLevel) {
                SteelTankBlock.updateTowerState(pLevel, be.getBlockPos(), false, false);
            }
			TFMG.LOGGER.debug("Distillation Tower? {}", be.isDistillationTower);
        }
        return InteractionResult.SUCCESS;
    }
}
