package com.drmangotea.tfmg.content.electricity.experimental.saved_data;

import com.drmangotea.tfmg.base.annotation.NothingNullByDefault;
import com.drmangotea.tfmg.content.electricity.experimental.ElectricalProperties;
import com.drmangotea.tfmg.content.electricity.experimental.RealElectricNetworkManager;
import com.drmangotea.tfmg.content.electricity.experimental.RealElectricalNetwork;
import com.drmangotea.tfmg.content.electricity.experimental.WireConnection;
import com.drmangotea.tfmg.content.electricity.experimental.simulation.*;
import com.drmangotea.tfmg.content.electricity.experimental.simulation.nodes.ConnectingElectricalNode;
import net.minecraft.core.BlockPos;
import com.drmangotea.tfmg.content.electricity.experimental.content.DirectionalElectricalProperties;
import com.drmangotea.tfmg.content.electricity.experimental.content.devices.base.ResistorProperties;
import com.drmangotea.tfmg.content.electricity.experimental.content.devices.electric_motor.ElectricMotorProperties;
import com.drmangotea.tfmg.content.electricity.experimental.content.devices.generators.creative_generator.CreativeGeneratorProperties;
import com.drmangotea.tfmg.content.electricity.experimental.content.devices.generators.three_phase_generator.ThreePhaseGeneratorProperties;
import com.drmangotea.tfmg.content.electricity.experimental.content.lights.LightProperties;
import com.drmangotea.tfmg.content.electricity.experimental.content.power_transmission.adapters.FourSlotAdapterProperties;
import com.drmangotea.tfmg.content.electricity.experimental.content.power_transmission.adapters.TwoSlotAdapterProperties;
import com.drmangotea.tfmg.content.electricity.experimental.content.power_transmission.cables.CableProperties;
import com.drmangotea.tfmg.content.electricity.experimental.content.power_transmission.energy_storage.accumulator_control_unit.AccumulatorControlUnitProperties;
import com.drmangotea.tfmg.content.electricity.experimental.content.power_transmission.switches.electric_switch.SwitchProperties;
import com.drmangotea.tfmg.content.electricity.experimental.content.power_transmission.switches.large_switch.LargeSwitchProperties;
import com.drmangotea.tfmg.content.electricity.experimental.content.power_transmission.transformers.large.LargeTransformerProperties;
import com.drmangotea.tfmg.content.electricity.experimental.content.power_transmission.transformers.small.TransformerProperties;
import com.drmangotea.tfmg.content.electricity.experimental.content.power_transmission.wires.ConnectorProperties;
import com.drmangotea.tfmg.content.electricity.experimental.simulation.ElectricalComponent;
import com.drmangotea.tfmg.content.electricity.experimental.simulation.IdealVoltageSource;
import com.drmangotea.tfmg.content.electricity.experimental.simulation.Resistance;
import com.drmangotea.tfmg.content.electricity.experimental.simulation.Transformer;
import com.drmangotea.tfmg.content.electricity.experimental.simulation.nodes.ConnectableElectricalNode;
import com.drmangotea.tfmg.content.electricity.experimental.simulation.nodes.ElectricalNode;
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

    @Override @NothingNullByDefault
    public CompoundTag save(CompoundTag compound, HolderLookup.Provider provider) {
        List<RealElectricalNetwork> list = RealElectricNetworkManager.networks.values().stream().toList();

        compound = new CompoundTag();

        for (RealElectricalNetwork network : list) {
            CompoundTag networkNBT = new CompoundTag();

            if (network.world instanceof ServerLevel serverLevel) {

                networkNBT.putString("Dimension", serverLevel.dimension().location().toString());

                CompoundTag members = new CompoundTag();
                networkNBT.putInt("Member Count", network.members.size());
                networkNBT.putInt("Node Count", network.totalNodes);
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
                            member.putDouble("Resistance " + resistance.localId, resistance.resistance);
                        }
                        if (component instanceof Transformer transformer) {
                            member.putDouble("Transformer_Ratio " + transformer.localId, transformer.turnsRatio);
                        }
                    }

                    List<Integer> connectDirections = new ArrayList<>();

                    for (Direction facing : properties.getConnectableDirections()) {
                        connectDirections.add(facing.get3DDataValue());
                    }
                    member.putIntArray("Connect Direction",connectDirections);


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

                    connectionTag.putDouble("Resistance", connection.resistance);
                    connectionTag.putBoolean("Should Render", connection.render);

                    connectionTag.put("Node1", connection.node1.save());
					connectionTag.put("Node2", connection.node2.save());

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

                    if (member.contains("direction")) {
                        direction = Direction.from3DDataValue(member.getInt("direction"));
                    }
                    int[] d = member.getIntArray("Connect Direction");
                    List<Direction> directions = new ArrayList<>();
                    for (int k : d) {
                        directions.add(Direction.from3DDataValue(k));
                    }

                    ElectricalProperties properties = getElectricalProperties(member.getInt("Property Id"), direction, directions, pos);

                    properties.position = pos;
                    network.members.put(pos, properties);
                    for (ElectricalComponent component : properties.components) {
                        if (component instanceof Resistance r) {
                            network.setResistance(pos, r.localId, member.getDouble("Resistance " + r.localId));
                        }
                        if (component instanceof IdealVoltageSource v) {
                            network.setVoltageGen(pos, member.getInt("Voltage " + v.localId));
                        }
                        if (component instanceof Transformer t) {
                            network.setTransformerRatio(pos, t.localId, member.getDouble("Transformer_Ratio " + t.localId));
                        }
                    }


                }
                int connectionCount = networkTag.getInt("Connection Count");
                for (int i = 0; i < connectionCount; i++) {
                    CompoundTag connection = networkTag.getCompound("connections").getCompound("connection " + i);
                    double resistance = connection.getDouble("Resistance");
					boolean shouldRender = connection.getBoolean("Should Render");
					
					ConnectableElectricalNode node1 = getNode(connection.getCompound("Node1"), network);
					ConnectableElectricalNode node2 = getNode(connection.getCompound("Node2"), network);
					
                    if (node1 != null && node2 != null) {
                        network.connections.add(new WireConnection(node1, node2, resistance, shouldRender));
                    }

                }
            }
        }

        return sd;
    }
	
	private static ConnectingElectricalNode getNode(CompoundTag tag, RealElectricalNetwork network) {
		ConnectingElectricalNode node = null;
		BlockPos pos = NbtUtils.readBlockPos(tag, "position").orElse(null);
		if (pos == null) return node;
		int id1 = tag.getInt("local id");
		for (ElectricalNode existingNode : network.getNodes(pos)) {
			if (existingNode.localId == id1 && existingNode instanceof ConnectingElectricalNode connectingNode) {
				node = connectingNode;
				node.pos = pos;
			}
		}
		return node;
	}

    public static ElectricalProperties getElectricalProperties(int id, Direction direction, List<Direction> d, BlockPos pos) {
		return switch (id) {
            case 1 -> new ThreePhaseGeneratorProperties(pos, direction);
            case 2 -> new ConnectorProperties(pos);
            case 3 -> new ResistorProperties(pos, direction);
            case 4 -> new CableProperties(pos, d);
            case 5 -> new FourSlotAdapterProperties(pos, direction);
            case 6 -> new TwoSlotAdapterProperties(pos, direction);
            case 7 -> new LightProperties(pos);
            case 8 -> new ElectricMotorProperties(pos, direction);
            case 9 -> new TransformerProperties(pos, direction);
            case 10 -> new CreativeGeneratorProperties(pos);
            case 11 -> new SwitchProperties(pos, direction);
            case 12 -> new LargeTransformerProperties(pos, direction);
            case 13 -> new AccumulatorControlUnitProperties(pos, direction);
            case 14 -> new LargeSwitchProperties(pos, direction);
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
