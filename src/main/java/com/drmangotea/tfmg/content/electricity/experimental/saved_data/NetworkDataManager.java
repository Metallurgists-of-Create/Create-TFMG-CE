package com.drmangotea.tfmg.content.electricity.experimental.saved_data;

import com.drmangotea.tfmg.content.electricity.experimental.RealElectricNetworkManager;
import com.drmangotea.tfmg.content.electricity.experimental.RealElectricalNetwork;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.LevelAccessor;

import java.util.ArrayList;
import java.util.List;

public class NetworkDataManager {
        public List<RealElectricalNetwork> list;
        private NetworkSavedData savedData;

        public NetworkDataManager() {
            list = new ArrayList<>();
            list.addAll(RealElectricNetworkManager.networks.values());
        }

        public void levelLoaded(LevelAccessor level) {
            MinecraftServer server = level.getServer();
            if (server == null || server.overworld() != level)
                return;
            savedData = null;
            loadNetworkData(server);
        }
		
        private void loadNetworkData(MinecraftServer server) {
            if (savedData != null)
                return;
            savedData = NetworkSavedData.load(server);
            list = savedData.getNetworks();
        }
		
        public void markDirty() {
            if (savedData != null)
                savedData.setDirty();
        }
}
