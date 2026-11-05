package com.drmangotea.tfmg.content.electricity;

import com.drmangotea.tfmg.TFMG;
import com.drmangotea.tfmg.base.lang.TFMGTexts;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import java.util.List;

import static com.drmangotea.tfmg.content.electricity.content.power_transmission.cables.AbstractCableBlockEntity.connectToNeighbors;

public interface IRealisticElectric extends IHaveMultimeterInformation {
	ElectricalProperties getProperties();

    BlockPos getPos();
	
    default RealElectricalNetwork getNetwork(Level level) {
        return RealElectricNetworkManager.getNetwork(level);
    }

    default RealElectricalNetwork getNetwork() {
        return getNetwork(getWorld());
    }

    default void updateNetwork(BlockPos pos) {
        RealElectricalNetwork network = RealElectricNetworkManager.getNetwork(getWorld());
        if (network == null)
            return;
        network.addMember(pos, getProperties());
        network.update();
        TFMG.ELECTRICAL_NETWORK_DATA.markDirty();
    }


    default void removeBlock() {
        RealElectricalNetwork network = RealElectricNetworkManager.getNetwork(getWorld());
        network.members.remove(this.getPos());
        network.removeConnections(this);
        if (this.getProperties() != null)
            network.nodes.removeAll(this.getProperties().nodes);


        TFMG.ELECTRICAL_NETWORK_DATA.markDirty();
        network.update();
    }


    default void onUpdated() {
    }

    default boolean makeMultimeterTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        TFMGTexts.header("multimeter").style(ChatFormatting.WHITE)
                .forGoggles(tooltip);

		/* TODO: Add getters or something like that
		TFMGTexts.Multimeter.resistance(resistance).forGoggles(tooltip);
		TFMGTexts.Multimeter.voltage(voltage).forGoggles(tooltip);
		TFMGTexts.Multimeter.current(current).forGoggles(tooltip);
		TFMGTexts.Multimeter.power(current*voltage).forGoggles(tooltip);
		*/

        return true;
    }

    default void onPlace() {
        this.updateNetwork(getPos());
        /*if (this instanceof ElectricSwitchBlockEntity)
            for (Direction facing : Direction.values()) {
                if (getWorld().getBlockEntity(getPos().relative(facing)) instanceof IRealisticElectric be) {
                    connectToNeighbors(getWorld(), be);
                }
            }


        if (!(this instanceof CableBlockEntity) && getProperties().cableConnectable()) {
            connectToNeighbors(getWorld(), this);
            updateNetwork(BlockPos.of(getPos()));
        }*/
		
		connectToNeighbors(getWorld(), this);
        this.updateNetwork(getPos());

    }

    Level getWorld();

}
