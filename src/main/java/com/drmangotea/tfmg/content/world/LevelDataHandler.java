package com.drmangotea.tfmg.content.world;

import com.drmangotea.tfmg.TFMG;
import com.drmangotea.tfmg.content.world.resevoir.FluidReservoirs;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.util.HashMap;
import java.util.Map;

public class LevelDataHandler {
    public static LevelDataHandler instance = new LevelDataHandler();

    public static final Map<ServerLevel, FluidReservoirs> fluidReservoirSaveHolder = new HashMap<>();

    public static FluidReservoirs getFluidReservoirs(ServerLevel level) {
        FluidReservoirs fluidReservoirs = fluidReservoirSaveHolder.get(level);
        if(fluidReservoirs==null){
            TFMG.LOGGER.error("Null Fluid Reservoir Data from Level : {}", level);
        }
        return fluidReservoirs != null ? fluidReservoirs : fluidReservoirSaveHolder.put(level, FluidReservoirs.get(level));
    }

    @SubscribeEvent
    public void onLevelLoad(LevelEvent.Load event) {
        if (!event.getLevel().isClientSide()  && event.getLevel() instanceof ServerLevel serverLevel)
            fluidReservoirSaveHolder.putIfAbsent(serverLevel, FluidReservoirs.get(serverLevel));
    }

    @SubscribeEvent
    public void onLevelSave(LevelEvent.Unload event) {
        if (!event.getLevel().isClientSide() && event.getLevel() instanceof ServerLevel serverLevel) {
            fluidReservoirSaveHolder.remove(serverLevel);
        }
    }
}
