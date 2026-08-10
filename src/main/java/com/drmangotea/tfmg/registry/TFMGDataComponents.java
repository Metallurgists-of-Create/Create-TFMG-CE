package com.drmangotea.tfmg.registry;

import java.util.function.UnaryOperator;

import com.drmangotea.tfmg.TFMG;
import com.drmangotea.tfmg.base.data_storage.CylinderFuels;
import com.drmangotea.tfmg.content.items.weapons.flamethrover.FlamethrowerFuel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.ApiStatus.Internal;

import com.mojang.serialization.Codec;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponentType.Builder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class TFMGDataComponents {
	public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, TFMG.MOD_ID);

	public static final DataComponentType<Integer> SPOOL_AMOUNT = register(
			"spool_amount",
			builder -> builder.persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT)
	);
	public static final DataComponentType<Integer> COIL_TURNS = register(
			"coil_turns",
			builder -> builder.persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT)
	);
	public static final DataComponentType<Integer> CONFIGURATION_WRENCH_NUMBER = register(
			"number",
			builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT)
	);
	public static final DataComponentType<Integer> LITHIUM_BLADE_TIMER = register(
			"timer",
			builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT)
	);
	public static final DataComponentType<FlamethrowerFuel> FLAMETHROWER = register(
			"flamethrower",
			builder -> builder.persistent(FlamethrowerFuel.CODEC).networkSynchronized(FlamethrowerFuel.STREAM_CODEC)
	);

	public static final DataComponentType<Integer> ACCUMULATOR_STORAGE = register(
			"storage",
			builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT)
	);

	public static final DataComponentType<Integer> RESISTANCE = register(
			"resistance",
			builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT)
	);
	public static final DataComponentType<Integer> AMOUNT = register(
			"amount",
			builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT)
	);
	public static final DataComponentType<BlockPos> POSITION = register(
			"position",
			builder -> builder.persistent(BlockPos.CODEC).networkSynchronized(BlockPos.STREAM_CODEC)
	);

	public static final DataComponentType<CylinderFuels> ENGINE_CYLINDER = register("engine_cylinder", CylinderFuels.CODEC, CylinderFuels.STREAM_CODEC);


	//Legacy components for remapping item data.
	public static final DataComponentType<String> FLAMETHROWER_FUEL = register("flamethrower_fuel", builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));
	public static final DataComponentType<CompoundTag> FUELS = register("fuels", builder -> builder.persistent(CompoundTag.CODEC).networkSynchronized(ByteBufCodecs.COMPOUND_TAG));

	private static <T> DataComponentType<T> register(String name, Codec<T> persistent, StreamCodec<? super RegistryFriendlyByteBuf, T> network) {
		return register(name, builder -> builder.persistent(persistent).networkSynchronized(network));
	}

	private static <T> DataComponentType<T> register(String name, UnaryOperator<Builder<T>> builder) {
		DataComponentType<T> type = builder.apply(DataComponentType.builder()).build();
		DATA_COMPONENTS.register(name, () -> type);
		return type;
	}

	@Internal
	public static void register(IEventBus modEventBus) {
		DATA_COMPONENTS.register(modEventBus);
	}
}
