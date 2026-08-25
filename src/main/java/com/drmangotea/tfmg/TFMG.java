package com.drmangotea.tfmg;

import com.drmangotea.tfmg.base.TFMGBoilerHeaters;
import com.drmangotea.tfmg.base.TFMGContraptions;
import com.drmangotea.tfmg.base.TFMGCreativeTabs;
import com.drmangotea.tfmg.base.TFMGRegistrate;
import com.drmangotea.tfmg.base.fluid.TFMGFluidInteractions;
import com.drmangotea.tfmg.config.TFMGConfigs;
import com.drmangotea.tfmg.content.decoration.pipes.TFMGPipes;
import com.drmangotea.tfmg.content.electricity.base.ElectricNetworkManager;
import com.drmangotea.tfmg.content.items.weapons.explosives.thermite_grenades.fire.TFMGColoredFires;
import com.drmangotea.tfmg.content.machinery.oil_processing.pumpjack.base.TestSavedDataManager;
import com.drmangotea.tfmg.datagen.TFMGDatagen;
import com.drmangotea.tfmg.registry.*;
import com.drmangotea.tfmg.worldgen.TFMGFeatures;
import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.Contract;
import org.slf4j.Logger;

import java.util.Random;


@Mod(TFMG.MOD_ID)
public class TFMG {
    public static final Random RANDOM = new Random();
    public static final String MOD_ID = "tfmg";
    public static final ElectricNetworkManager NETWORK_MANAGER = new ElectricNetworkManager();
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final TestSavedDataManager DEPOSITS = new TestSavedDataManager();

    public static final TFMGRegistrate REGISTRATE = TFMGRegistrate.create(MOD_ID)
            .setTooltipModifierFactory(item ->
                    new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                            .andThen(TooltipModifier.mapNull(KineticStats.create(item)))
            );


    public TFMG(IEventBus modEventBus, ModContainer modContainer) {
        ModLoadingContext modLoadingContext = ModLoadingContext.get();
        REGISTRATE.registerEventListeners(modEventBus);
        TFMGSoundEvents.prepare();
        TFMGElectrodes.init();
        TFMGMixerModes.init();
        TFMGVatOperations.init();
        TFMGCableTypes.init();
        TFMGEngineTypes.init();
        TFMGDisplaySources.init();
        TFMGCreativeTabs.register(modEventBus);
        TFMGBlocks.init();
        TFMGPipes.init();
        TFMGBlockEntities.init();
        TFMGItems.init();
        TFMGEntityTypes.init();
        TFMGPartialModels.init();
        TFMGFluids.init();
        TFMGMenuTypes.init();
        TFMGEncasedBlocks.init();
        TFMGPaletteBlocks.init();

        TFMGParticleTypes.register(modEventBus);

        TFMGDataComponents.register(modEventBus);
        TFMGMobEffects.register(modEventBus);
        TFMGRecipeTypes.register(modEventBus);
        TFMGColoredFires.register(modEventBus);
        TFMGFeatures.register(modEventBus);
        TFMGMountedStorageTypes.register();

        modEventBus.addListener(TFMG::onRegister);
        TFMGPackets.register();
        TFMGConfigs.register(modLoadingContext, modContainer);
        modEventBus.addListener(EventPriority.HIGHEST, TFMGDatagen::gatherDataHighPriority);
        modEventBus.addListener(EventPriority.LOWEST, TFMGDatagen::gatherData);
        modEventBus.addListener(TFMGSoundEvents::register);
        modEventBus.addListener(TFMG::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(TFMGCreativeTabs::addCreative);
    }

    @SuppressWarnings("deprecation")
    private void clientSetup(final FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(TFMGColoredFires.GREEN_FIRE.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(TFMGColoredFires.BLUE_FIRE.get(), RenderType.cutout());
    }

    //This Javadoc here pissed off the compiler. I'm giving it a gold star!
    /**
     * fluid interaction and firebox heating
     */
    public static void commonSetup(final FMLCommonSetupEvent event) {
        TFMGFluidInteractions.registerFluidInteractions();
        event.enqueueWork(TFMGBoilerHeaters::registerDefaults);
    }

    public static void onRegister(final RegisterEvent event) {
        TFMGContraptions.prepare();
    }

    @Contract("_ -> new")
    public static ResourceLocation asResource(String path) {
        if (path.contains(":")) {
            return ResourceLocation.tryParse(path);
        }
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static TFMGRegistrate registrate() {
        return REGISTRATE;
    }
}
