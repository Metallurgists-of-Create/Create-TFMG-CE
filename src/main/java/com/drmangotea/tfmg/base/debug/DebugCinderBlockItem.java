package com.drmangotea.tfmg.base.debug;

import com.drmangotea.tfmg.TFMG;
import com.drmangotea.tfmg.content.decoration.tanks.steel.SteelTankBlock;
import com.drmangotea.tfmg.content.decoration.tanks.steel.SteelTankBlockEntity;
import com.drmangotea.tfmg.content.electricity.IRealisticElectric;
import com.drmangotea.tfmg.content.electricity.RealElectricNetworkManager;
import com.drmangotea.tfmg.content.electricity.RealElectricalNetwork;
import com.drmangotea.tfmg.content.electricity.content.devices.resistor.DebugResistorBlockEntity;
import com.drmangotea.tfmg.content.electricity.content.lights.ElectricLightBlockEntity;
import com.drmangotea.tfmg.content.electricity.content.measuring_instruments.VoltMeterBlockEntity;
import com.drmangotea.tfmg.content.electricity.content.power_transmission.cables.cable_hubs.CableHubBlockEntity;
import com.drmangotea.tfmg.content.electricity.content.power_transmission.energy_storage.accumulator_control_unit.AccumulatorControlUnitBlockEntity;
import com.drmangotea.tfmg.content.electricity.content.power_transmission.energy_storage.accumulator_rack.AccumulatorRackBlockEntity;
import com.drmangotea.tfmg.content.electricity.packets.NetworkLoadPacket;
import com.drmangotea.tfmg.content.electricity.simulation.IdealVoltageSource;
import com.drmangotea.tfmg.content.electricity.simulation.Resistance;
import com.simibubi.create.Create;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import static com.drmangotea.tfmg.content.electricity.content.power_transmission.cables.AbstractCableBlockEntity.connectToNeighbors;


public class DebugCinderBlockItem extends Item {
    public DebugCinderBlockItem(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return true;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {

        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (level.getBlockEntity(pos) instanceof AccumulatorRackBlockEntity be) {
            be.updateStorage();


        }
        if (level.getBlockEntity(pos) instanceof VoltMeterBlockEntity be) {
            RealElectricalNetwork network = RealElectricNetworkManager.getNetwork(be.getWorld());
            TFMG.LOGGER.debug("voltmeter: "+be.voltage);
            Resistance resistor = network.getResistance(be.getPos(),0);
            if(resistor !=null){
                TFMG.LOGGER.debug("nodes "+resistor.nodeA.networkId+" "+resistor.nodeB.networkId);
            }
            if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
                NetworkLoadPacket packet = new NetworkLoadPacket(RealElectricNetworkManager.networks.values().stream().toList());
                CatnipServices.NETWORK.sendToClient(serverPlayer, packet);
            }
        }
        if (level.getBlockEntity(pos) instanceof AccumulatorControlUnitBlockEntity be) {
            RealElectricalNetwork network = RealElectricNetworkManager.getNetwork(be.getWorld());

            network.update();

            IdealVoltageSource source = network.getVoltageSource(be.getPos(),0);
            if(source == null){
                return InteractionResult.PASS;
            }

            TFMG.LOGGER.debug("generator: "+source.amplitude+" "+source.phaseOffset);
        }
        if (level.getBlockEntity(pos) instanceof DebugResistorBlockEntity be) {

            RealElectricalNetwork network = RealElectricNetworkManager.getNetwork(be.getWorld());

            network.setResistance(be, 0, Create.RANDOM.nextInt(5));

            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof IRealisticElectric be && false) {
            //if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
            //    NetworkLoadPacket packet = new NetworkLoadPacket(RealElectricNetworkManager.networks.values().stream().toList());
            //    CatnipServices.NETWORK.sendToClient(serverPlayer, packet);
            //}

            RealElectricalNetwork network = RealElectricNetworkManager.getNetwork(be.getWorld());

            // network.setVoltageGen(be, Create.RANDOM.nextInt(700));


            TFMG.LOGGER.debug("Member count: " + network.members.size());
            TFMG.LOGGER.debug("Node Count: " + network.nodes.size());
            TFMG.LOGGER.debug("Connection Count: " + network.connections.size());
            TFMG.LOGGER.debug("Resistor Count: " + network.resistors.size());
            TFMG.LOGGER.debug("This Block Node Count: " + be.getProperties().nodes.size());



        }


        if (level.getBlockEntity(pos) instanceof SteelTankBlockEntity be) {
            if (!context.getPlayer().isCrouching()) {
                //SteelTankBlock.updateTowerState(be.getLevel(), be.getBlockPos(), true, false);
                // be.updateTemperature();
                TFMG.LOGGER.debug(String.valueOf(be.isDistillationTower));
                //be.sendData();
                //be.getControllerBE().sendData();
            } else {
                SteelTankBlock.updateTowerState(be.getLevel(), be.getBlockPos(), false, false);
                TFMG.LOGGER.debug(String.valueOf(be.isDistillationTower));
            }
        }
        return InteractionResult.SUCCESS;
    }
}
