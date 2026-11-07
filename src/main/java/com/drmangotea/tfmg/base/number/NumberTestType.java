package com.drmangotea.tfmg.base.number;

import com.mojang.serialization.MapCodec;

public record NumberTestType<T extends NumberTest>(MapCodec<T> codec) {
}
