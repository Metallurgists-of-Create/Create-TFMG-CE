package com.drmangotea.tfmg.content.machinery.oil_processing;

import com.drmangotea.tfmg.base.lang.TFMGLang;
import com.drmangotea.tfmg.content.world.resevoir.FluidReservoir;
import com.drmangotea.tfmg.registry.TFMGDataAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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

        if (!level.getChunk(pos).hasData(TFMGDataAttachments.FLUID_RESERVOIR)) {
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        FluidReservoir reservoir = level.getChunk(pos).getData(TFMGDataAttachments.FLUID_RESERVOIR);

        int oilReserves = reservoir.getReserves();
        if (level.isClientSide && player != null)
            player.displayClientMessage(TFMGLang.translateDirect("oil_hammer.reserves", oilReserves)
                    .withStyle(ChatFormatting.YELLOW), true);

        return InteractionResult.SUCCESS;
    }
}
