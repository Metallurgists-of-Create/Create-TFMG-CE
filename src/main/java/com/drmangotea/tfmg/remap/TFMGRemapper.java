package com.drmangotea.tfmg.remap;

import com.drmangotea.tfmg.TFMG;
import com.drmangotea.tfmg.TFMGRegistries;
import com.drmangotea.tfmg.base.data_storage.CylinderFuels;
import com.drmangotea.tfmg.content.machinery.vat.electrode_holder.electrode.Electrode;
import com.drmangotea.tfmg.registry.TFMGDataComponents;
import com.drmangotea.tfmg.registry.TFMGEngineFuelTypes;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@EventBusSubscriber
public class TFMGRemapper {

    /**
     * Remapped objects. Fairly straight forward.
     */
    private static final List<Remap> remaps = new ArrayList<>();

    static {
        remaps.add(Remap.block("copper_encased_brass_pipe", TFMG.asResource("encased_brass_pipe")));
        remaps.add(Remap.block("copper_encased_steel_pipe", TFMG.asResource("encased_steel_pipe")));
        remaps.add(Remap.block("copper_encased_aluminum_pipe", TFMG.asResource("encased_aluminum_pipe")));
        remaps.add(Remap.block("copper_encased_cast_iron_pipe", TFMG.asResource("encased_cast_iron_pipe")));
        remaps.add(Remap.block("copper_encased_plastic_pipe", TFMG.asResource("encased_plastic_pipe")));

        remaps.add(Remap.block("heavy_casing_encased_shaft", TFMG.asResource("heavy_encased_shaft")));
        remaps.add(Remap.block("heavy_casing_encased_steel_cogwheel", TFMG.asResource("heavy_encased_steel_cogwheel")));
        remaps.add(Remap.block("heavy_casing_encased_large_steel_cogwheel", TFMG.asResource("heavy_encased_large_steel_cogwheel")));
        remaps.add(Remap.block("heavy_casing_encased_aluminum_cogwheel", TFMG.asResource("heavy_encased_aluminum_cogwheel")));
        remaps.add(Remap.block("heavy_casing_encased_large_aluminum_cogwheel", TFMG.asResource("heavy_encased_large_aluminum_cogwheel")));

        remaps.add(Remap.item("lit_lithium_blade", TFMG.asResource("lithium_blade")));
    }

    @SubscribeEvent
    public static void remap(RegisterEvent event) {
        Registry<?> registry = event.getRegistry();
        remaps.forEach((remap -> remap.remap(registry)));
    }

    private static boolean sentChemicaMessage = false;

    @SubscribeEvent
    public static void modifyDefaultComponents(ModifyDefaultComponentsEvent event) {
        for (Item item : event.getAllItems().toList()) {
            var key = BuiltInRegistries.ITEM.getKey(item);
            if (key.getNamespace().equals("chemica")) {
                boolean didRemap = false;
                CylinderFuels component = switch (key.getPath()) {
                    case "biodiesel_engine_cylinder" -> new CylinderFuels(List.of(TFMGEngineFuelTypes.BIODIESEL));
                    case "ethanol_engine_cylinder" -> new CylinderFuels(List.of(TFMGEngineFuelTypes.ETHANOL));
                    case "high_cetane_engine_cylinder" -> new CylinderFuels(List.of(TFMGEngineFuelTypes.HIGH_CETANE_DIESEL));
                    case "high_octane_engine_cylinder" -> new CylinderFuels(List.of(TFMGEngineFuelTypes.HIGH_OCTANE_GASOLINE));
                    case "hydrogen_turbine_blade" -> new CylinderFuels(List.of(TFMGEngineFuelTypes.HYDROGEN_FUEL));
                    default -> CylinderFuels.EMPTY;
                };
                if (!component.isEmpty()) {
                    event.modify(item, (c) -> c.set(TFMGDataComponents.ENGINE_CYLINDER, component));
                    didRemap = true;
                }
                if (key.getPath().equals("platinum_electrode")) {
                    Optional<Holder.Reference<Electrode>> electrodeHolder = TFMGRegistries.ELECTRODE_REGISTRY.getHolder(TFMG.asResource("chemica:electrode"));
                    electrodeHolder.ifPresent(holder -> event.modify(item, (c) -> c.set(TFMGDataComponents.ELECTRODE, new Electrode.Stored(holder))));
                    didRemap = true;
                }
                if (didRemap && !sentChemicaMessage) {
                    TFMG.LOGGER.info("[TFMG Remapper] Remapped old Chemica default components");
                    sentChemicaMessage = true;
                }
            }
        }
    }

    public static void remapComponents(ItemStack stack, RegistryAccess registryAccess) {
        if (ComponentRemapper.engineCylinder(stack, registryAccess)) {
            TFMG.LOGGER.info("[TFMG Remapper] Remapped old Engine Cylinder components");
        }
        if (ComponentRemapper.flamethrower(stack, registryAccess)) {
            TFMG.LOGGER.info("[TFMG Remapper] Remapped old Flamethrower components");
        }
    }
}
