package com.drmangotea.tfmg.content.machinery.oil_processing;

import com.drmangotea.tfmg.base.lang.TFMGLang;
import com.drmangotea.tfmg.content.world.LevelDataHandler;
import com.drmangotea.tfmg.content.world.resevoir.FluidReservoirs;
import com.drmangotea.tfmg.registry.TFMGBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class OilHammerItem extends Item {
    public OilHammerItem(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();

        FluidReservoirs reservoirs = null;

        if (level instanceof ServerLevel serverLevel) {
            reservoirs = LevelDataHandler.getFluidReservoirs(serverLevel);
        }

        if (reservoirs != null) {
            for(int i = 0; i < 300; i++) {
                BlockPos posToCheck = pos.below(i);
                if(level.getBlockState(posToCheck).is(TFMGBlocks.OIL_DEPOSIT.get())) {
                    if(reservoirs.getReservoirFor(posToCheck) == null)
                        return InteractionResult.SUCCESS;
                    int oilReserves = reservoirs.getReservoirFor(posToCheck).getReserves();

                    if (level.isClientSide && player != null)
                        player.displayClientMessage(TFMGLang.translateDirect("oil_hammer.reserves", oilReserves)
                                .withStyle(ChatFormatting.YELLOW), true);

                    return InteractionResult.SUCCESS;
                }
            }
        }

        return InteractionResult.SUCCESS;
    }
}
