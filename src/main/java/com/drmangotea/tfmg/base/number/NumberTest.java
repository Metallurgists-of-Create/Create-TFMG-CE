package com.drmangotea.tfmg.base.number;

import com.drmangotea.tfmg.TFMGRegistries;
import com.mojang.serialization.Codec;
import net.createmod.catnip.data.Couple;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;

import java.util.List;

public interface NumberTest {
    Codec<NumberTest> CODEC = Codec.lazyInitialized(TFMGRegistries.NUMBER_TEST_TYPE_REGISTRY::byNameCodec).dispatch("type", NumberTest::getType, NumberTestType::codec);

    boolean test(double value, RandomSource random);

    Component formatText(Couple<Double> limits, RandomSource random);

    Couple<Double> getBounds(Couple<Double> limits, RandomSource random);

    NumberTestType<?> getType();
}
