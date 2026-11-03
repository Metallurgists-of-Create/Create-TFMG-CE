package com.drmangotea.tfmg.content.electricity.content.power_transmission.energy_storage.accumulator_control_unit;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.List;

public class AccumulatorControlUnitScrollBehaviour extends ScrollValueBehaviour {
    public AccumulatorControlUnitScrollBehaviour(Component label, SmartBlockEntity be, ValueBoxTransform slot) {
        super(label, be, slot);
    }

    @Override
    public void setValueSettings(Player player, ValueSettings valueSetting, boolean ctrlHeld) {
        int value = valueSetting.value();
        int multiplier = switch (valueSetting.row()) {
            case 0 -> 1;
            case 1 -> 121;
            default -> 122*122;
        };
        if (!valueSetting.equals(getValueSettings()))
            playFeedbackSound(this);
        setValue(Math.max(2, Math.max(1, value) * multiplier));
    }
    @Override
    public ValueSettings getValueSettings() {
        int row = 0;
        int value = this.value;

        if (value > 120 * 121) {
            value = value / (122*122);
            row = 2;
        } else if (value > 120) {
            value = value / 121;
            row = 1;
        }

        return new ValueSettings(row, value);
    }

    @Override
    public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {

        List<Component> rows = new ArrayList<>();
        rows.add(Component.literal("L1"));
        rows.add(Component.literal("L2"));
        rows.add(Component.literal("L3"));

        return new ValueSettingsBoard(label, 120, 10, rows,
                new ValueSettingsFormatter(this::formatSettings));
    }
    public MutableComponent formatSettings(ValueSettings settings) {
        int value = Math.max(1, settings.value());
        return Component.literal(switch (settings.row()) {
            case 0 -> value+ "V";
            case 1 -> value+ "V";
            default -> value+ "V";
        });
    }

    @Override
    public String formatValue() {
        ValueSettings settings =getValueSettings();
        int value = Math.max(1, settings.value());
        return switch (settings.row()) {
            case 0 -> value+ "V";
            case 1 -> value+ "V";
            default -> value+ "V";
        };
    }
}
