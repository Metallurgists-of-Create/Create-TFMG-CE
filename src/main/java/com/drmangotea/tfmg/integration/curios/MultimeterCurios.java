package com.drmangotea.tfmg.integration.curios;

import com.simibubi.create.compat.Mods;
import net.minecraft.world.entity.player.Player;

public class MultimeterCurios {

    public static boolean isWearingMultimeter(Player player) {
        if (!Mods.CURIOS.isLoaded()) return false;
        return LoadedOnly.isWearingMultimeter(player);
    }

    public static class LoadedOnly {

        public static boolean isWearingMultimeter(Player player) {
            //Do stuff here
            return false;
        }
    }
}
