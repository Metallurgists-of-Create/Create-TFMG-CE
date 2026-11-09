package com.drmangotea.tfmg.base.number;

import com.drmangotea.tfmg.TFMGRegistries;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;

import javax.annotation.Nullable;

public interface NumberTest {
    Codec<NumberTest> CODEC = Codec.lazyInitialized(TFMGRegistries.NUMBER_TEST_TYPE_REGISTRY::byNameCodec).dispatch("type", NumberTest::getType, NumberTestType::codec);

    boolean test(double value, RandomSource random);

    Component formatText(Bounds limits);

    @Nullable
	Bounds getBounds();

    NumberTestType<?> getType();
	
	record Bounds (
		double min,
		double max,
		boolean inclusive
	) {
		public Bounds(double min, double max) { this(min, max, true); }
		
		public Bounds union (Bounds other) { return union(other, true); }
		
		public Bounds union (Bounds other, boolean inclusive) { return union(this, other, inclusive); }
		
		public static Bounds union (Bounds A, Bounds B, boolean inclusive) {
			return new Bounds (Math.min(A.min(), B.min()), Math.max(A.max(), B.max()), inclusive);
		}
		
		public Bounds intersection (Bounds other) { return intersection(other, true); }
		
		public Bounds intersection (Bounds other, boolean inclusive) { return intersection(this, other, inclusive); }
		
		public static Bounds intersection (Bounds A, Bounds B, boolean inclusive) {
			if (A.max() < B.min() || B.max() < A.min())
				return null; //no intersection
			
			return new Bounds (Math.min(A.min(), B.min()), Math.max(A.max(), B.max()), inclusive);
		}
	}
}
