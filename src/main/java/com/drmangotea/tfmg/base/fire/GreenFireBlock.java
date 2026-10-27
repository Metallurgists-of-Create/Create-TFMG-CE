package com.drmangotea.tfmg.base.fire;

import com.drmangotea.tfmg.base.annotation.NothingNullByDefault;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BaseFireBlock;

@NothingNullByDefault
public class GreenFireBlock extends BaseTFMGFireBlock {
    public static final MapCodec<GreenFireBlock> CODEC = simpleCodec(GreenFireBlock::new);

    public GreenFireBlock(Properties p_53425_) {
        super(p_53425_, 1.0F);
    }

    @Override
    protected MapCodec<? extends BaseFireBlock> codec() {
        return CODEC;
    }
	
	protected static BaseTFMGFireBlock getBlock() {
		return TFMGColoredFires.GREEN_FIRE.get();
	}
}
