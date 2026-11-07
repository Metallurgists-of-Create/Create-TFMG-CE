package com.drmangotea.tfmg.base.number;

import com.drmangotea.tfmg.TFMGRegistries;
import com.mojang.serialization.Codec;
import net.minecraft.util.RandomSource;

public interface NumberTest {
    Codec<NumberTest> CODEC = Codec.lazyInitialized(TFMGRegistries.NUMBER_TEST_TYPE_REGISTRY::byNameCodec).dispatch("type", NumberTest::getType, NumberTestType::codec);

    boolean test(double value, RandomSource random);

    NumberTestType<?> getType();
}
