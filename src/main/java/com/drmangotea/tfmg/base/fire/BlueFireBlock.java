package com.drmangotea.tfmg.base.fire;

import com.drmangotea.tfmg.base.annotation.NothingNullByDefault;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BaseFireBlock;

@NothingNullByDefault
public class BlueFireBlock extends BaseTFMGFireBlock {
    public static final MapCodec<BlueFireBlock> CODEC = simpleCodec(BlueFireBlock::new);

    public BlueFireBlock(Properties p_53425_) {
        super(p_53425_, 1.0F);
    }

    @Override
    protected MapCodec<? extends BaseFireBlock> codec() {
        return CODEC;
    }
	
	protected static BaseTFMGFireBlock getBlock() {
		return TFMGColoredFires.BLUE_FIRE.get();
	}
}
