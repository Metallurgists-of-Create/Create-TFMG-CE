package com.drmangotea.tfmg.remap;

import com.drmangotea.tfmg.TFMG;
import com.drmangotea.tfmg.TFMGRegistries;
import com.drmangotea.tfmg.base.data_storage.CylinderFuels;
import com.drmangotea.tfmg.content.engines.fuels.EngineFuelType;
import com.drmangotea.tfmg.content.items.weapons.flamethrover.FlamethrowerFuel;
import com.drmangotea.tfmg.registry.TFMGDataComponents;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ComponentRemapper {


    public static boolean engineCylinder(ItemStack stack, RegistryAccess registryAccess) {
        if (stack.has(TFMGDataComponents.FUELS)) {
            CompoundTag fuels = stack.get(TFMGDataComponents.FUELS);
            if(fuels == null || fuels.isEmpty())
                return false;
            List<ResourceKey<EngineFuelType>> validKeys = new ArrayList<>();
            for(String fuel : fuels.getAllKeys()) {
                Optional<ResourceKey<EngineFuelType>> key = engineFuelKey(fuel, registryAccess);
                key.ifPresent(validKeys::add);
            }
            if (!validKeys.isEmpty()) {
                stack.set(TFMGDataComponents.ENGINE_CYLINDER, new CylinderFuels(validKeys));
                stack.remove(TFMGDataComponents.FUELS);
                stack.remove(TFMGDataComponents.FUEL_TAGS);
                return true;
            }
        }
        return false;
    }

    public static boolean flamethrower(ItemStack stack, RegistryAccess registryAccess) {
        if(!stack.has(TFMGDataComponents.FLAMETHROWER)) {
            stack.set(TFMGDataComponents.FLAMETHROWER, FlamethrowerFuel.EMPTY);
            return true;
        }
        if (stack.has(TFMGDataComponents.FLAMETHROWER_FUEL) && stack.has(TFMGDataComponents.AMOUNT)) {
            int fuelAmount = stack.getOrDefault(TFMGDataComponents.AMOUNT, 0);
            String fuelType = stack.getOrDefault(TFMGDataComponents.FLAMETHROWER_FUEL, "fallback");
            if (fuelType.isEmpty()) fuelType = "fallback";
            FlamethrowerFuel fuel = FlamethrowerFuel.createForLegacy(registryAccess, fuelType, fuelAmount);
            stack.set(TFMGDataComponents.FLAMETHROWER, fuel);
            stack.remove(TFMGDataComponents.FLAMETHROWER_FUEL); stack.remove(TFMGDataComponents.AMOUNT);
            return true;
        }
        return false;
    }


    private static Optional<ResourceKey<EngineFuelType>> engineFuelKey(String name, RegistryAccess registryAccess) {
        ResourceKey<EngineFuelType> key = ResourceKey.create(TFMGRegistries.ENGINE_FUEL_TYPE, TFMG.asResource(name));
        Optional<Holder.Reference<EngineFuelType>> type = registryAccess.lookupOrThrow(TFMGRegistries.ENGINE_FUEL_TYPE).get(key);
        if (type.isEmpty()) {
            return chemicaEngineFuelKey(name, registryAccess);
        }
        return type.flatMap(Holder.Reference::unwrapKey);
    }

    private static Optional<ResourceKey<EngineFuelType>> chemicaEngineFuelKey(String name, RegistryAccess registryAccess) {
        ResourceKey<EngineFuelType> key = ResourceKey.create(TFMGRegistries.ENGINE_FUEL_TYPE, ResourceLocation.fromNamespaceAndPath("chemica", name));
        Optional<Holder.Reference<EngineFuelType>> type = registryAccess.lookupOrThrow(TFMGRegistries.ENGINE_FUEL_TYPE).get(key);
        return type.flatMap(Holder.Reference::unwrapKey);
    }
}
