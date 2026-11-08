package com.drmangotea.tfmg.base.number;

import com.drmangotea.tfmg.base.lang.TFMGLang;
import com.drmangotea.tfmg.registry.TFMGNumberTests;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.createmod.catnip.data.Couple;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;

public class NumberTests {

    public static Any any() {
        return Any.INSTANCE;
    }

    public static Exact exact(double target) {
        return new Exact(target);
    }

    public static Less less(double target, boolean inclusive) {
        return new Less(target, inclusive);
    }

    public static Less less(double target) {
        return new Less(target);
    }

    public static Greater greater(double target, boolean inclusive) {
        return new Greater(target, inclusive);
    }

    public static Greater greater(double target) {
        return new Greater(target);
    }

    public static Between between(double min, double max, boolean inclusive) {
        return new Between(min, max, inclusive);
    }

    public static Between between(double min, double max) {
        return new Between(min, max);
    }

    public enum Any implements NumberTest {
        INSTANCE;

        public static final MapCodec<Any> CODEC = MapCodec.unit(INSTANCE);

        @Override
        public boolean test(double value, RandomSource random) {
            return true;
        }

        @Override
        public Component formatText(Couple<Double> limits, RandomSource random) {
            return TFMGLang.translate("number_test.any").component();
        }

        @Override
        public Couple<Double> getBounds(Couple<Double> limits, RandomSource random) {
            return limits;
        }

        @Override
        public NumberTestType<?> getType() {
            return TFMGNumberTests.ANY.get();
        }
    }

    public record Exact(double target) implements NumberTest {
        public static final MapCodec<Exact> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
                Codec.DOUBLE.fieldOf("target").forGetter(Exact::target)
        ).apply(instance, Exact::new));

        @Override
        public boolean test(double value, RandomSource random) {
            return value == target();
        }

        @Override
        public Component formatText(Couple<Double> limits, RandomSource random) {
            return TFMGLang.translate("number_test.exact", target()).component();
        }

        @Override
        public Couple<Double> getBounds(Couple<Double> limits, RandomSource random) {
            return Couple.create(target(), target());
        }

        @Override
        public NumberTestType<?> getType() {
            return TFMGNumberTests.EXACT.get();
        }
    }

    public record Less(double target, boolean inclusive) implements NumberTest {
        public static final MapCodec<Less> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
                Codec.DOUBLE.fieldOf("target").forGetter(Less::target),
                Codec.BOOL.optionalFieldOf("inclusive", true).forGetter(Less::inclusive)
        ).apply(instance, Less::new));

        public Less(double target) {
            this(target, true);
        }

        @Override
        public boolean test(double value, RandomSource random) {
            if (inclusive)
                return value <= target();
            return value < target();
        }

        @Override
        public Couple<Double> getBounds(Couple<Double> limits, RandomSource random) {
            return Couple.create(Math.min(target(), limits.getFirst()), Math.min(target(), limits.getSecond()));
        }

        @Override
        public Component formatText(Couple<Double> limits, RandomSource random) {
            return TFMGLang.translate("number_test.less" + (inclusive() ? ".inclusive" : ""), target()).component();
        }

        @Override
        public NumberTestType<?> getType() {
            return TFMGNumberTests.LESS.get();
        }
    }

    public record Greater(double target, boolean inclusive) implements NumberTest {
        public static final MapCodec<Greater> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
                Codec.DOUBLE.fieldOf("target").forGetter(Greater::target),
                Codec.BOOL.optionalFieldOf("inclusive", true).forGetter(Greater::inclusive)
        ).apply(instance, Greater::new));

        public Greater(double target) {
            this(target, true);
        }

        @Override
        public boolean test(double value, RandomSource random) {
            if (inclusive)
                return value >= target();
            return value > target();
        }

        @Override
        public Component formatText(Couple<Double> limits, RandomSource random) {
            return TFMGLang.translate("number_test.greater" + (inclusive() ? ".inclusive" : ""), target()).component();
        }

        @Override
        public Couple<Double> getBounds(Couple<Double> limits, RandomSource random) {
            return Couple.create(Math.max(target(), limits.getFirst()), Math.max(target(), limits.getSecond()));
        }

        @Override
        public NumberTestType<?> getType() {
            return TFMGNumberTests.GREATER.get();
        }
    }

    public record Between(double min, double max, boolean inclusive) implements NumberTest {
        public static final MapCodec<Between> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
                Codec.DOUBLE.fieldOf("min").forGetter(Between::min),
                Codec.DOUBLE.fieldOf("max").forGetter(Between::max),
                Codec.BOOL.optionalFieldOf("inclusive", true).forGetter(Between::inclusive)
        ).apply(instance, Between::new));

        public Between(double min, double max) {
            this(min, max, true);
        }

        @Override
        public boolean test(double value, RandomSource random) {
            if (inclusive)
                return min() <= value && max() >= value;
            return min() < value && max() > value;
        }

        @Override
        public Component formatText(Couple<Double> limits, RandomSource random) {
            return TFMGLang.translate("number_test.between" + (inclusive() ? ".inclusive" : ""), min(), max()).component();
        }

        @Override
        public Couple<Double> getBounds(Couple<Double> limits, RandomSource random) {
            double min = inclusive ? min() : Math.nextUp(min());
            double max = inclusive ? max() : Math.nextDown(max());
            return Couple.create(min, max);
        }

        @Override
        public NumberTestType<?> getType() {
            return TFMGNumberTests.BETWEEN.get();
        }
    }
}
