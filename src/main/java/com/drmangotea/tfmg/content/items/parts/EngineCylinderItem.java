package com.drmangotea.tfmg.content.items.parts;


import com.drmangotea.tfmg.TFMG;
import com.drmangotea.tfmg.TFMGRegistries;
import com.drmangotea.tfmg.base.data_storage.CylinderFuels;
import com.drmangotea.tfmg.content.engines.fuel.EngineFuelType;
import com.drmangotea.tfmg.registry.TFMGDataComponents;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EngineCylinderItem extends Item {

    public EngineCylinderItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (remapOldComponents(stack, level.registryAccess())) {
            TFMG.LOGGER.info("[TFMG Remapper] Remapped old Engine Cylinder components");
        }
    }

    private boolean remapOldComponents(ItemStack stack, RegistryAccess registryAccess) {
        if (stack.has(TFMGDataComponents.FUELS)) {
            CompoundTag fuels = stack.get(TFMGDataComponents.FUELS);
            if(fuels == null || fuels.isEmpty())
                return false;
            List<ResourceKey<EngineFuelType>> validKeys = new ArrayList<>();
            for(String fuel : fuels.getAllKeys()) {
                Optional<ResourceKey<EngineFuelType>> key = validateKey(fuel, registryAccess);
                key.ifPresent(validKeys::add);
            }
            if (!validKeys.isEmpty()) {
                stack.set(TFMGDataComponents.ENGINE_CYLINDER, new CylinderFuels(validKeys));
                stack.remove(TFMGDataComponents.FUELS);
                return true;
            }
        }
        return false;
    }

    private Optional<ResourceKey<EngineFuelType>> validateKey(String name, RegistryAccess registryAccess) {
        ResourceKey<EngineFuelType> key = ResourceKey.create(TFMGRegistries.ENGINE_FUEL_TYPE, TFMG.asResource(name));
        Optional<Holder.Reference<EngineFuelType>> type = registryAccess.lookupOrThrow(TFMGRegistries.ENGINE_FUEL_TYPE).get(key);
        return type.flatMap(Holder.Reference::unwrapKey);
    }
}
