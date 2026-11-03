package com.drmangotea.tfmg.content.electricity;

import com.drmangotea.tfmg.content.electricity.packets.NetworkLoadPacket;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RealElectricNetworkManager {
    public static Map<LevelAccessor, RealElectricalNetwork> networks = new HashMap<>();

    public static Map<LevelAccessor, Pair<RealElectricalNetwork, NetworkCalculationData>> updateQueue = new HashMap<>();

    public void onLoadWorld(LevelAccessor world) {
        networks.put(world, new RealElectricalNetwork(world));

    }

    public void onUnloadWorld(LevelAccessor world) {
        networks.remove(world);
    }

    public static RealElectricalNetwork getNetwork(Level level) {
        return networks.get(level);
    }

    public static void handleNetworkUpdate(ServerTickEvent.Post event) {

    }

    public static LevelAccessor getWorldFromNetwork(RealElectricalNetwork network) {

        for (Map.Entry<LevelAccessor, RealElectricalNetwork> entry : networks.entrySet()) {
            if (Objects.equals(network, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static void playerLogin(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkLoadPacket packet = new NetworkLoadPacket(RealElectricNetworkManager.networks.values().stream().toList());
            CatnipServices.NETWORK.sendToClient(serverPlayer, packet);

            RealElectricalNetwork network = RealElectricNetworkManager.getNetwork(player.level());
            network.update();


        }
    }


}
