package com.drmangotea.tfmg.content.electricity.base;

import com.drmangotea.tfmg.TFMG;
import com.drmangotea.tfmg.base.TFMGUtils;
import com.drmangotea.tfmg.base.lang.TFMGLang;
import com.drmangotea.tfmg.base.lang.TFMGTexts;
import com.drmangotea.tfmg.content.electricity.connection.CableHubBlockEntity;
import com.drmangotea.tfmg.content.electricity.connection.cables.CableConnection;
import com.drmangotea.tfmg.content.electricity.connection.cables.CableConnectorBlockEntity;
import com.drmangotea.tfmg.content.electricity.measurement.MultimeterItem;
import com.drmangotea.tfmg.content.electricity.network.large_switch.LargeSwitchBlockEntity;
import com.drmangotea.tfmg.content.electricity.network.transformer.large.LargeTransformerBlockEntity;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

/**
 * data and actions for electric blocks
 */
public interface IElectric extends IHaveGoggleInformation {

    /**
     * block's world position
     */
    BlockPos getPos();

    int FE_TRANSFER_RATE = 4048;

    /**
     * world the blocks is in
     */
    LevelAccessor getLevelAccessor();

    /**
     * @return true if the block is marked as removed
     */
    default boolean destroyed() {
        return getData().destroyed();
    }

    /**
     * checks if this block is part of a valid network
     *
     * @return block's network if valid, newly created network for this block otherwise
     */
    default ElectricalNetwork getOrCreateElectricNetwork() {
        if (getLevelAccessor().getBlockEntity(BlockPos.of(getData().electricalNetworkId)) instanceof IElectric) {
            return TFMG.NETWORK_MANAGER.getOrCreateNetworkFor((IElectric) getLevelAccessor().getBlockEntity(BlockPos.of(getData().electricalNetworkId)));
        } else {
            ElectricNetworkManager.networks.computeIfAbsent(getLevelAccessor(), lvl -> new HashMap<>()).remove(getData().electricalNetworkId);
            return TFMG.NETWORK_MANAGER.getOrCreateNetworkFor(this);
        }
    }

    /**
     * tells the block which sides of it can be attached to the grid
     */
    default boolean hasElectricitySlot(Direction direction) {
        return true;
    }


    /**
     * initialization, called when the block is placed
     */
    default void onPlaced() {
        if (getLevelAccessor() instanceof ServerLevel serverLevel)
            CatnipServices.NETWORK.sendToClientsTrackingChunk(serverLevel, new ChunkPos(getPos()), new ConnectNeighborsPacket(getPos()));
        ElectricalNetwork network = TFMG.NETWORK_MANAGER.getOrCreateNetworkFor(this);
        setNetwork(getPos().asLong());
        getData().electricalNetworkId = getPos().asLong();
        network.add(this);


        getData().checkForLoopsNextTick = true;
        getOrCreateElectricNetwork().checkForLoops(getPos());
        /// ////


        updateNextTick();

        onConnected();
        sendStuff();

    }

    /**
     * manages removal of this block after it is destroyed
     */
    default void onRemoved() {
        this.getData().destroyed = true;
        for (Direction d : Direction.values()) {
            if (hasElectricitySlot(d))
                if (getLevelAccessor().getBlockEntity(getPos().relative(d)) instanceof IElectric be && be.hasElectricitySlot(d.getOpposite())) {
                    ElectricNetworkManager.networks.computeIfAbsent(getLevelAccessor(), lvl -> new HashMap<>())
						.remove(be.getPos().asLong());
                    be.setNetwork(be.getPos().asLong());
                    be.onPlaced();
                    be.updateNextTick();
                }
        }
        if (getData().electricalNetworkId != getPos().asLong())
            getOrCreateElectricNetwork().getMembers().remove(this);
//
        if (getData().electricalNetworkId == getPos().asLong())
            ElectricNetworkManager.networks.computeIfAbsent(getLevelAccessor(), lvl -> new HashMap<>())
                    .remove(getData().getId());
    }

    /**
     * loads data
     */
    default void readElectricity(CompoundTag compound, boolean clientPacket) {
        if (!clientPacket)
            getData().connectNextTick = true;
    }

    /**
     * handles action that need to happen every tick and checks for scheduled updates
     */
    default void tickElectricity() {

        int oldEnergyGiven = getData().energyGiven;
        if (getData().tickUntilConnectFE == -1)
            sendEnergy();

        if (powerStateChanged()) {
            updateNextTick();
        }

        if (oldEnergyGiven != getData().energyGiven) {
            updateNextTick();
        }

        if (getData().tickUntilConnectFE >= 0) {
            getData().tickUntilConnectFE--;
        }

        if (!getData().scheduledActions.isEmpty()) {
            getData().scheduledActions.forEach(m -> m.accept(null));
        }

        if (getData().checkForLoopsNextTick) {
            getOrCreateElectricNetwork().checkForLoops(getPos());
            getData().checkForLoopsNextTick = false;
        }

        if (getData().connectNextTick) {
            onPlaced();
            getData().connectNextTick = false;
        }

        if (getData().updateNextTick) {
            updateNetwork();
            getData().updateNextTick = false;
        }

        if (getData().updatePowerNextTick) {
            updateUnpowered(new ArrayList<>());
            getData().updatePowerNextTick = false;
        }
        if (getData().setVoltageNextTick) {
            setVoltage(getData().voltageSupply);
            getData().setVoltageNextTick = false;
        }
    }

    default boolean powerStateChanged() {
        float currentPowerUsage = getPowerUsage();
        float currentPowerGeneration = powerGeneration();
        int currentVoltageGeneration = voltageGeneration();
        int currentVoltage = getData().getVoltage();

        if (Float.isNaN(getData().lastPowerUsage)) {
            getData().lastPowerUsage = currentPowerUsage;
            getData().lastPowerGeneration = Math.round(currentPowerGeneration);
            getData().lastVoltageGeneration = currentVoltageGeneration;
            getData().lastVoltage = currentVoltage;
            return false;
        }

        boolean changed = Math.abs(getData().lastPowerUsage - currentPowerUsage) > 0.001F ||
                getData().lastPowerGeneration != Math.round(currentPowerGeneration) ||
                getData().lastVoltageGeneration != currentVoltageGeneration ||
                getData().lastVoltage != currentVoltage;

        if (changed) {
            getData().lastPowerUsage = currentPowerUsage;
            getData().lastPowerGeneration = Math.round(currentPowerGeneration);
            getData().lastVoltageGeneration = currentVoltageGeneration;
            getData().lastVoltage = currentVoltage;
        }

        return changed;
    }

    default void sendEnergy() {

        if (getData().energyTaken > 0)
            return;

        if (getData().energyGiven != 0)
            getData().energyGiven = 0;


        if (getData().voltage == 0) {

            // if(getData().energyGiven>0)
            //     updateNextTick();
            getData().energyGiven = 0;

            return;
        }


        int voltage = getData().getVoltage();

        float maximumPossibleEnergy = getData().networkResistance == 0 ? 0 : ((float) getData().networkPowerGeneration) * 0.9f;

        //maximumPossibleEnergy /=1000;
        for (IEnergyStorage capability : getData().energyOutputs.values()) {
            //   int energyGiven = capability.receiveEnergy((int) Math.min(voltage,maximumPossibleEnergy), false);

            float spaceFilled = (float) capability.getEnergyStored() /capability.getMaxEnergyStored();
            if(spaceFilled<=0.9)
                getData().waitingForNextCharge = false;
            if(spaceFilled>0.9&&getData().waitingForNextCharge) {

                continue;
            }

            if(spaceFilled==1){
                getData().waitingForNextCharge = true;
            }



            int energyGiven = capability.receiveEnergy((int) Math.min(voltage * 4, maximumPossibleEnergy), getData().notEnoughPower);
            getData().energyGiven += energyGiven;

        }


    }

    default boolean isCable() {
        return false;
    }

    /**
     * handles actions that need to repeat every second
     */
    default void lazyTickElectricity() {

        if (getPowerUsage() > getData().networkPowerGeneration && !getData().notEnoughPower)
            onPlaced();


        if (getData().failTimer >= 4) {

            this.blockFail();
            if (getLevelAccessor() instanceof ServerLevel serverLevel)
                CatnipServices.NETWORK.sendToClientsTrackingChunk(serverLevel, new ChunkPos(getPos()), new ElectricalBlockFailPacket(getPos()));
            getData().failTimer = 0;
            sendStuff();
        } else if ((getData().voltage > getMaxVoltage() && getMaxVoltage() > 0) || (getCurrent() > getMaxCurrent() && getMaxCurrent() > 0) || ((getData().highestCurrent > getMaxCurrent() && getMaxCurrent() > 0) && isCable())) {

            getData().failTimer++;
        }

    }


    default int getMaxVoltage() {
        return 1000;
    }

    default int getMaxCurrent() {
        return 0;
    }

    /**
     * handles connecting blocks into a network
     */
    default void onConnected() {


        BlockPos pos = getPos();
        if (this instanceof CableConnectorBlockEntity be) {
            for (CableConnection connection : be.connections) {
                if (getLevelAccessor().getBlockEntity(connection.pos1().equals(getPos()) ? connection.pos2() : connection.pos1()) instanceof CableConnectorBlockEntity otherBe) {

                    if (!otherBe.destroyed()) {
                        if (!getOrCreateElectricNetwork().members.contains(otherBe))
                            getOrCreateElectricNetwork().members.add(otherBe);
                        if (otherBe.getData().getId() != getData().getId()) {
                            otherBe.setNetwork(getData().getId());
                            otherBe.onConnected();
                            if (!getLevelAccessor().isClientSide())
                                sendStuff();
                        }
                    }

                }
            }
        }
        for (Direction d : Direction.values()) {
            if (hasElectricitySlot(d))
                if (getLevelAccessor().getBlockEntity(pos.relative(d)) instanceof IElectric be) {
                    if (be.hasElectricitySlot(d.getOpposite())) {
                        if (!be.destroyed()) {
                            getOrCreateElectricNetwork().add(be);
                            if (be.getData().getId() != getData().getId()) {
                                be.setNetwork(getData().getId());
                                be.onConnected();
                                if (!getLevelAccessor().isClientSide())
                                    sendStuff();
                            }
                        }
                    } else if (be.getData().getId() != getData().getId()) {
                        be.updateNextTick();
                    }
                }
        }

        sendStuff();

    }

    /**
     * tells blocks when the network doesn't have enough power
     */
    default void updateUnpowered(List<BlockPos> alreadyChecked) {
        alreadyChecked.add(getPos());
        updateNextTick();

        if (this instanceof CableConnectorBlockEntity connectorBE) {
            for (CableConnection connection : connectorBE.connections) {
                if (getLevelAccessor().getBlockEntity(connection.pos1().equals(getPos()) ? connection.pos2() : connection.pos1()) instanceof CableConnectorBlockEntity be2 && !alreadyChecked.contains(be2.getBlockPos())) {
                    be2.updateUnpowered(alreadyChecked);
                }
            }
        }

        for (Direction direction : Direction.values()) {
            if (getLevelAccessor().getBlockEntity(getPos().relative(direction)) instanceof IElectric be && !alreadyChecked.contains(be.getPos())) {
                be.updateUnpowered(alreadyChecked);
            }
        }
    }

    default void doActionNextTick(Consumer<Integer> method) {
        getData().scheduledActions.add(method);
    }

    /**
     * Adds Multimeter Tooltip to UI
     */
    @Override
    default boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (Minecraft.getInstance().player == null
                || !MultimeterItem.isHeldByPlayer(Minecraft.getInstance().player))
            return false;
        return makeMultimeterTooltip(tooltip, isPlayerSneaking);
    }

    /**
     * Populates the Multimeter Tooltip from the IElectrics' Data
     * @param tooltip The Tooltip to Populate
     * @param isPlayerSneaking Whether the Player is Sneaking
     * @return Whether the Tooltip should be displayed
     */
    default boolean makeMultimeterTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        TFMGTexts.header("multimeter").style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        // Warn about overvoltage/undercurrent
        if (getMaxVoltage() != 0 && getMaxVoltage() * 0.8f < getData().getVoltage())
            TFMGLang.translate("multimeter.approaching_overvoltage").add(TFMGLang.text("(" + TFMGUtils.formatUnits(getMaxVoltage(), "V" + ")"))).style(ChatFormatting.RED).forGoggles(tooltip);
        if (getMaxCurrent() != 0 && getMaxCurrent() * 0.8f < getCurrent())
            TFMGLang.translate("multimeter.approaching_overcurrent").add(TFMGLang.text("(" + TFMGUtils.formatUnits(getMaxCurrent(), "A" + ")"))).style(ChatFormatting.RED).forGoggles(tooltip);

        // Warn the player that network is overloaded
        if (getData().notEnoughPower) TFMGTexts.Multimeter.notEnoughPower().forGoggles(tooltip, 1);

        // Populate voltage generated, power generated
        if (voltageGeneration() > 0) {
            TFMGTexts.Multimeter.powerGenerated(powerGeneration()).forGoggles(tooltip, 1);
            TFMGTexts.Multimeter.voltageGenerated(voltageGeneration()).forGoggles(tooltip, 1);
            TFMGTexts.Multimeter.separator().forGoggles(tooltip);
        }

        // Display Resistance, Voltage and Current
        if (resistance() != 0&&!(this instanceof CableConnectorBlockEntity)&&!(this instanceof CableHubBlockEntity))
            TFMGTexts.Multimeter.resistance(voltageGeneration() > 0 ? getGeneratorResistance() : resistance()).forGoggles(tooltip, 1);
        TFMGTexts.Multimeter.voltage(getData().getVoltage()).forGoggles(tooltip, 1);
        TFMGTexts.Multimeter.current(resistance() == 0 ? getData().highestCurrent : getCurrent()).forGoggles(tooltip, 1);

        // How much power is this IElectric using?
        if (resistance() != 0)
            TFMGTexts.Multimeter.power(getPowerUsage()).forGoggles(tooltip, 1);

        // How much power is this IElectric giving to the network, if it is taking power at all?
        if (getData().energyGiven > 0) {
            TFMGTexts.Multimeter.sendingFE(getData().energyGiven).forGoggles(tooltip, 1);
        }

        // How much power is this IElectric taking from the network, if it is taking power at all?
        if (getData().energyTakenPerTick > 0) {
            TFMGTexts.Multimeter.takingFE(getData().energyTakenPerTick).forGoggles(tooltip, 1);
        }

        // If the player is sneaking, add total network generation and consumption
        if (isPlayerSneaking) {
            TFMGTexts.Multimeter.separator().forGoggles(tooltip);
            TFMGTexts.Multimeter.networkGeneration(getNetworkPowerGeneration()).forGoggles(tooltip, 1);
            TFMGTexts.Multimeter.networkConsumption(getNetworkPowerUsage()).forGoggles(tooltip, 1);
        }

        return true;
    }


    default void checkForFEOutputs(List<Direction> directions) {
        int oldOutputs = getData().energyOutputs.size();
        getData().energyOutputs = new HashMap<>();
        for (Direction direction : directions) {

            BlockPos pos = getPos().relative(direction);
            if (getLevelAccessor() instanceof Level level) {
                IEnergyStorage energyCapability = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, level.getBlockState(pos), level.getBlockEntity(pos), direction.getOpposite());
                if (energyCapability != null) {
                    getData().energyOutputs.put(direction, energyCapability);
                }
            }
        }
    }


    /**
     * contains data related to electricity
     */
    ElectricBlockValues getData();


    /**
     * @return true if the network has enough power
     */
    default boolean canWork() {
        return !getData().notEnoughPower;
    }

    default void blockFail() {

        // getLevelAccessor().destroyBlock(getPos(), false);

    }

    default float getPowerUsage() {
        return  (getData().getVoltage() * getCurrent());
    }

    default int getNetworkPowerUsage(IElectric blocked) {
        float power = 0;
        for (IElectric member : getOrCreateElectricNetwork().members)
            if (member.getPos().asLong() != blocked.getPos().asLong()) {
                power += member.getPowerUsage();
            } else blocked.updateNextTick();
        return (int) power;
    }

    default float getNetworkPowerUsage() {
        float power = 0;
        for (IElectric member : getOrCreateElectricNetwork().members)
            power += member.getPowerUsage();
        return power;
    }


    default int getNetworkPowerGeneration() {
        int power = 0;
        for (IElectric member : getOrCreateElectricNetwork().members)

            power += member.powerGeneration();
        return power;
    }

    default void onNetworkChanged(int oldVoltage, float oldPower) {
        if (getData().getsOutsidePower && oldPower != getPowerUsage()) {

            //    doActionNextTick(i->recalculateNetworkResistance());
        }
    }

    default float getGeneratorResistance() {
        if (getData().voltageSupply == 0)
            return 0;

        if ((float) getData().networkPowerGeneration * getNetworkResistance() == 0)
            return 0;

        return powerGeneration() / (float) getData().networkPowerGeneration * getNetworkResistance();
    }

    default float getGeneratorLoad() {
        if (getNetworkPowerUsage() == 0)
            return 0;
        return (float) powerGeneration() / (float) getData().networkPowerGeneration * getNetworkPowerUsage();
    }

    default int getBlocksConnectedToNetworkCount(long id) {

        int count = 0;

        for (IElectric member : getOrCreateElectricNetwork().members) {
            if (member instanceof VoltageAlteringBlockEntity be && be.getControlledBlock() != null) {
                if (be.getControlledBlock().getData().getId() == id)
                    count++;
            }
            if (member instanceof LargeSwitchBlockEntity be && be.getControlledBlock() != null) {
                if (be.getControlledBlock().getData().getId() == id)
                    count++;
            }
            if (member instanceof LargeTransformerBlockEntity be && be.getControlledBlock() != null) {
                if (be.getControlledBlock().getData().getId() == id)
                    count++;
            }

        }

        return count;

    }


    default float resistance() {
        return 100000;
    }


    default int voltageGeneration() {

        int voltageGeneration = 0;

        for (Direction direction : Direction.values()) {
            if (hasElectricitySlot(direction)) {

                if (getLevelAccessor().getBlockEntity(getPos().relative(direction)) instanceof VoltageAlteringBlockEntity be)
                    if (be.getData().getId() != getData().getId())
                        if (be.getData().getVoltage() != 0)
                            if (be.hasElectricitySlot(direction)) {
                                voltageGeneration = Math.max(voltageGeneration, be.getOutputVoltage());
                                getData().getsOutsidePower = true;
                            }
            }
        }

        if (voltageGeneration == 0)
            getData().getsOutsidePower = false;

        return voltageGeneration;
    }

    default void recalculateNetworkResistance() {
        float resistance = 0;

        List<IElectric> members = getOrCreateElectricNetwork().members;
        for (IElectric member : members) {
            if (member.resistance() != 0)
                resistance += 1f / member.resistance();
        }
        for (IElectric member : members) {
            if (resistance != 0)
                member.setNetworkResistance(1f / resistance);
        }

    }

    default float powerGeneration() {

        float powerGeneration = 0;


        for (Direction direction : Direction.values()) {
            if (hasElectricitySlot(direction)) {

                if (getLevelAccessor().getBlockEntity(getPos().relative(direction)) instanceof VoltageAlteringBlockEntity be && be.canWork()) {

                    if (be.getData().getId() != getData().getId())
                        if (be.getData().getVoltage() != 0)
                            if (be.hasElectricitySlot(direction)) {
                                powerGeneration = Math.max(powerGeneration, be.getPowerUsage()) + 1;
                                powerGeneration = Math.min(powerGeneration, be.getMaxPowerOutput());
                                if (powerGeneration > be.getNetworkPowerGeneration()) {

                                    powerGeneration = 0;

                                    be.data.updatePowerNextTick = true;
                                }
                            }
                }
            }
        }
////
        //TFMG.LOGGER.debug("mnauch "+powerGeneration);
        return powerGeneration;

        //return 0;
    }

    default float getNetworkResistance() {
        return getData().networkResistance;
    }


    default boolean networkUndersupplied() {
        return getNetworkPowerUsage() > getData().networkPowerGeneration;
    }


    default float getCurrent() {
        return getData().getVoltage() == 0 || resistance() == 0 ? 0 : ((float) getData().getVoltage() / (float) resistance());
    }

    default void updateNextTick() {
        getData().updateNextTick = true;
    }

    default void updateNetwork() {
        getOrCreateElectricNetwork().updateNetwork();
        if (getLevelAccessor() instanceof ServerLevel serverLevel)
            CatnipServices.NETWORK.sendToClientsTrackingChunk(serverLevel, new ChunkPos(getPos()), new NetworkUpdatePacket(getPos()));
        sendStuff();
    }

    void sendStuff();


    default void setVoltage(int newVoltage) {
        getData().voltage = newVoltage;
    }


    default void setNetworkResistance(float newUsage) {
        getData().networkResistance = newUsage;
    }


    default void setNetwork(long network) {
        getData().electricalNetworkId = network;
        if (network != getPos().asLong())
            ElectricNetworkManager.networks.computeIfAbsent(getLevelAccessor(), lvl -> new HashMap<>())
                    .remove(getPos().asLong());
    }
}
