package com.drmangotea.tfmg.registry;

import com.drmangotea.tfmg.TFMG;
import com.drmangotea.tfmg.TFMGRegistries;
import com.drmangotea.tfmg.base.number.NumberTest;
import com.drmangotea.tfmg.base.number.NumberTestType;
import com.drmangotea.tfmg.base.number.NumberTests;
import com.mojang.serialization.MapCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

public class TFMGNumberTests {
    public static final DeferredRegister<NumberTestType<?>> NUMBER_TESTS = DeferredRegister.create(TFMGRegistries.NUMBER_TEST_TYPE, TFMG.MOD_ID);

    public static final Supplier<NumberTestType<NumberTests.Any>> ANY = register("any", NumberTests.Any.CODEC);
    public static final Supplier<NumberTestType<NumberTests.Flat>> FLAT = register("flat", NumberTests.Flat.CODEC);
    public static final Supplier<NumberTestType<NumberTests.Less>> LESS = register("less", NumberTests.Less.CODEC);
    public static final Supplier<NumberTestType<NumberTests.Greater>> GREATER = register("greater", NumberTests.Greater.CODEC);
    public static final Supplier<NumberTestType<NumberTests.Between>> BETWEEN = register("between", NumberTests.Between.CODEC);

    private static <P extends NumberTest> DeferredHolder<NumberTestType<? extends NumberTest>, NumberTestType<P>> register(String name, MapCodec<P> codec) {
        return NUMBER_TESTS.register(name, () -> new NumberTestType<>(codec));
    }

    @ApiStatus.Internal
    public static void register(IEventBus modEventBus) {
        NUMBER_TESTS.register(modEventBus);
    }
}
