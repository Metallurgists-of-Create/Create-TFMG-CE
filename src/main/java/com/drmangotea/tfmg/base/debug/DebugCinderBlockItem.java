package com.drmangotea.tfmg.base.debug;

import com.drmangotea.tfmg.TFMG;
import com.drmangotea.tfmg.base.annotation.NothingNullByDefault;
import com.drmangotea.tfmg.content.decoration.tanks.steel.SteelTankBlock;
import com.drmangotea.tfmg.content.decoration.tanks.steel.SteelTankBlockEntity;
import com.drmangotea.tfmg.content.electricity.experimental.IRealisticElectric;
import com.drmangotea.tfmg.content.electricity.experimental.RealElectricNetworkManager;
import com.drmangotea.tfmg.content.electricity.experimental.RealElectricalNetwork;
import com.drmangotea.tfmg.content.electricity.experimental.content.devices.generators.three_phase_generator.ThreePhaseGeneratorBlockEntity;
import com.drmangotea.tfmg.content.electricity.experimental.content.devices.resistor.DebugResistorBlockEntity;
import com.drmangotea.tfmg.content.electricity.base.IElectric;
import com.drmangotea.tfmg.content.electricity.connection.cables.CableConnectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

@NothingNullByDefault
public class DebugCinderBlockItem extends Item {
    public DebugCinderBlockItem(Properties p) {
        super(p);
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return true;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
		Player player = context.getPlayer();
		BlockEntity blockEntity = level.getBlockEntity(pos);
		
		if (blockEntity instanceof CableConnectorBlockEntity be) {
			TFMG.LOGGER.info("{}:\n {}", pos, be.connections);
		}

        if (blockEntity instanceof ThreePhaseGeneratorBlockEntity be) {
            RealElectricalNetwork network = RealElectricNetworkManager.getNetwork(be.getLevel());


        }
        if (blockEntity instanceof DebugResistorBlockEntity be) {

            RealElectricalNetwork network = RealElectricNetworkManager.getNetwork(be.getWorld());

            network.setResistance(be, 0, TFMG.RANDOM.nextInt(5));

            return InteractionResult.SUCCESS;
        }
        if (blockEntity instanceof IRealisticElectric be&&false) {
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
            // TFMG.LOGGER.debug("Voltage gen: " + be.getVoltageGeneration().getFirst().getFirst().getFirst());


            // network.connections.forEach(c -> {
            //     TFMG.LOGGER.debug("Connection1  " + c.node1().getPosition().x() + c.node1().getPosition().y() + c.node1().getPosition().z());
            //     TFMG.LOGGER.debug("Connection2  " + c.node2().getPosition().x() + c.node2().getPosition().y() + c.node2().getPosition().z());
            // });

            if (player != null && player.isCrouching()) {
                //  TFMG.ELECTRICAL_NETWORK_DATA.markDirty();
                network.update();
            }
		}


        if (player != null && blockEntity instanceof IElectric be) {
			if(player.isCrouching()){
				be.recalculateNetworkResistance();
			} else {
				be.updateNextTick();
				TFMG.LOGGER.debug("Network at {} with size {}", BlockPos.of(be.getData().electricalNetworkId), be.getOrCreateElectricNetwork().members.size());
			}
        }

        if (blockEntity instanceof SteelTankBlockEntity be) {
            if (player != null && player.isCrouching() && be.getLevel() instanceof Level pLevel) {
                SteelTankBlock.updateTowerState(pLevel, be.getBlockPos(), false, false);
            }
			TFMG.LOGGER.debug("Distillation Tower? {}", be.isDistillationTower);
        }
        return InteractionResult.SUCCESS;
    }
}
