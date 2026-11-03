package com.drmangotea.tfmg.content.electricity.packets;


import com.drmangotea.tfmg.content.electricity.content.power_transmission.energy_storage.accumulator_control_unit.AccumulatorControlUnitBlockEntity;
import com.drmangotea.tfmg.registry.TFMGPackets;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.networking.BlockEntityDataPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class UpdateAccumulatorPacket extends BlockEntityDataPacket<SmartBlockEntity> {

    protected final int voltage;

    public static final StreamCodec<ByteBuf, UpdateAccumulatorPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, packet -> packet.pos,
            ByteBufCodecs.INT, packet -> packet.voltage,
            UpdateAccumulatorPacket::new
    );


    public UpdateAccumulatorPacket(BlockPos pos,int voltage) {
        super(pos);
        this.voltage = voltage;
    }

    @Override
    protected void handlePacket(SmartBlockEntity blockEntity) {

        if (blockEntity instanceof AccumulatorControlUnitBlockEntity be) {
            AccumulatorControlUnitBlockEntity.updateVoltage(be,voltage);
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return TFMGPackets.UPDATE_ACCUMULATOR;
    }
}
