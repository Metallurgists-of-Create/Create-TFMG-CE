package com.drmangotea.tfmg.content.electricity.experimental.saved_data;

import com.drmangotea.tfmg.content.electricity.experimental.ElectricalProperties;
import com.drmangotea.tfmg.content.electricity.experimental.RealElectricNetworkManager;
import com.drmangotea.tfmg.content.electricity.experimental.RealElectricalNetwork;
import com.drmangotea.tfmg.content.electricity.experimental.WireConnection;
import com.drmangotea.tfmg.content.electricity.experimental.blocks.ConnectorProperties;
import com.drmangotea.tfmg.content.electricity.experimental.blocks.DebugResistorProperties;
import com.drmangotea.tfmg.content.electricity.experimental.blocks.DirectionalElectricalProperties;
import com.drmangotea.tfmg.content.electricity.experimental.blocks.ThreePhaseGeneratorProperties;
import com.drmangotea.tfmg.content.electricity.experimental.simulation.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.List;

public class NetworkSavedData extends SavedData {

    private List<RealElectricalNetwork> list = new ArrayList<>();

    public NetworkSavedData() {}

    @Override
    public CompoundTag save(CompoundTag compound, HolderLookup.Provider provider) {


        List<RealElectricalNetwork> list = RealElectricNetworkManager.networks.values().stream().toList();

        compound = new CompoundTag();

        for (RealElectricalNetwork network : list) {
            CompoundTag networkNBT = new CompoundTag();

            if (network.world instanceof ServerLevel serverLevel) {

                networkNBT.putString("Dimension", serverLevel.dimension().location().toString());

                CompoundTag members = new CompoundTag();
                networkNBT.putInt("Member Count", network.members.size());
                networkNBT.putInt("Node Count",network.totalNodes);
                for (int i = 0; i < network.members.size(); i++) {
                    CompoundTag member = new CompoundTag();
                    ElectricalProperties properties = network.members.values().stream().toList().get(i);
                    BlockPos pos = network.members.keySet().stream().toList().get(i);

                    member.put("Position", NbtUtils.writeBlockPos(pos));
                    member.putInt("Property Id", properties.getId());
                    for (ElectricalComponent component : properties.components) {
                        if (component instanceof IdealVoltageSource source) {
                            member.putInt("Voltage " + source.id, (int) source.amplitude);
                        }
                        if (component instanceof Resistance resistance) {
                            member.putInt("Resistance " + resistance.localId, (int) resistance.resistance);
                        }
                    }
                    if (properties instanceof DirectionalElectricalProperties p)
                        member.putInt("direction", p.direction.get3DDataValue());

                    members.put("member " + i, member);
                }
                networkNBT.put("blocks", members);
                ///
                CompoundTag connections = new CompoundTag();
                networkNBT.putInt("Connection Count", network.connections.size());
                for (int i = 0; i < network.connections.size(); i++) {
                    CompoundTag connectionTag = new CompoundTag();

                    WireConnection connection = network.connections.get(i);

                    connectionTag.putDouble("Resistance", connection.resistance());

                    connectionTag.put("Node1", connection.node1().save());
					connectionTag.put("Node2", connection.node2().save());

                    connections.put("connection " + i, connectionTag);
                }

                networkNBT.put("connections", connections);

                compound.put(serverLevel.dimension().location().toLanguageKey(), networkNBT);

            }
        }

        return compound;
    }

    public List<RealElectricalNetwork> getNetworks() {
        return list;
    }


    public static NetworkSavedData load(CompoundTag compound, HolderLookup.Provider registries) {
        NetworkSavedData sd = new NetworkSavedData();

        List<RealElectricalNetwork> list = RealElectricNetworkManager.networks.values().stream().toList();
        for (RealElectricalNetwork network : list) {
            if (network.world instanceof ServerLevel serverLevel) {
                CompoundTag networkTag = compound.getCompound(serverLevel.dimension().location().toLanguageKey());
                network.totalNodes = networkTag.getInt("Node Count");
                int memberCount = networkTag.getInt("Member Count");
                for (int i = 0; i < memberCount; i++) {
                    CompoundTag member = networkTag.getCompound("blocks").getCompound("member " + i);
					BlockPos pos = NbtUtils.readBlockPos(member, "Position").get();

                    Direction direction = Direction.NORTH;

                    if(member.contains("direction")){
                        direction = Direction.from3DDataValue(member.getInt("direction"));
                    }
                    ElectricalProperties properties = getElectricalProperties(member.getInt("Property Id"), pos, direction);

                    properties.position = pos;
                    network.members.put(pos, properties);

                }
                int connectionCount = networkTag.getInt("Connection Count");
                for (int i = 0; i < connectionCount; i++) {
                    CompoundTag connection = networkTag.getCompound("connections").getCompound("connection " + i);
                    double resistance = connection.getDouble("Resistance");
					
					ConnectingElectricalNode node1 = getNode(connection.getCompound("Node1"), network);
					ConnectingElectricalNode node2 = getNode(connection.getCompound("Node2"), network);

                    if (node1 != null && node2 != null) {
                        network.connections.add(new WireConnection(node1, node2, resistance));
                    }

                }
            }
        }

        return sd;
    }
	
	private static ConnectingElectricalNode getNode(CompoundTag tag, RealElectricalNetwork network) {
		ConnectingElectricalNode node = null;
		BlockPos pos = NbtUtils.readBlockPos(tag, "position").get();
		int id1 = tag.getInt("local id");
		for (ElectricalNode existingNode : network.getNodes(pos)) {
			if (existingNode.localId == id1 && existingNode instanceof ConnectingElectricalNode connectingNode) {
				node = connectingNode;
				node.pos = pos;
			}
		}
		return node;
	}

    public static ElectricalProperties getElectricalProperties(int id, BlockPos pos, Direction direction) {
		return switch (id) {
            case 1 -> new ThreePhaseGeneratorProperties(pos, direction);
            case 2 -> new ConnectorProperties(pos);
            case 3 -> new DebugResistorProperties(pos, direction);
            default -> new ElectricalProperties(pos);

        };
    }

    public static Factory<NetworkSavedData> factory() {
        return new Factory<>(NetworkSavedData::new, NetworkSavedData::load);
    }


    public static NetworkSavedData load(MinecraftServer server) {
        return server.overworld()
                .getDataStorage()
                .computeIfAbsent(factory(), "tfmg_networks");
    }


}
