package com.drmangotea.tfmg.registry;

import com.drmangotea.tfmg.TFMG;
import com.drmangotea.tfmg.content.electricity.ElectricalProperties;
import com.drmangotea.tfmg.content.electricity.ElectricalPropertiesType;
import com.drmangotea.tfmg.content.electricity.content.devices.base.ResistorProperties;
import com.drmangotea.tfmg.content.electricity.content.devices.electric_motor.ElectricMotorProperties;
import com.drmangotea.tfmg.content.electricity.content.devices.generators.creative_generator.CreativeGeneratorProperties;
import com.drmangotea.tfmg.content.electricity.content.devices.generators.three_phase_generator.ThreePhaseGeneratorProperties;
import com.drmangotea.tfmg.content.electricity.content.lights.LightProperties;
import com.drmangotea.tfmg.content.electricity.content.power_transmission.adapters.FourSlotAdapterProperties;
import com.drmangotea.tfmg.content.electricity.content.power_transmission.adapters.TwoSlotAdapterProperties;
import com.drmangotea.tfmg.content.electricity.content.power_transmission.cables.CableProperties;
import com.drmangotea.tfmg.content.electricity.content.power_transmission.energy_storage.accumulator_control_unit.AccumulatorControlUnitProperties;
import com.drmangotea.tfmg.content.electricity.content.power_transmission.switches.electric_switch.SwitchProperties;
import com.drmangotea.tfmg.content.electricity.content.power_transmission.switches.large_switch.LargeSwitchProperties;
import com.drmangotea.tfmg.content.electricity.content.power_transmission.transformers.large.LargeTransformerProperties;
import com.drmangotea.tfmg.content.electricity.content.power_transmission.transformers.small.TransformerProperties;
import com.drmangotea.tfmg.content.electricity.content.power_transmission.wires.ConnectorProperties;
import com.drmangotea.tfmg.content.electricity.content.power_transmission.wires.UnwirableConnectorProperties;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


import static com.drmangotea.tfmg.TFMGRegistries.ELECTRIC_TYPE;

//TODO: Shift to use Registrate? Or maybe not.
//TODO: Proper names!
public class TFMGElectricalProperties {
	static final DeferredRegister<ElectricalPropertiesType<?>> PROPERTIES = DeferredRegister.create(ELECTRIC_TYPE, TFMG.MOD_ID);
	
	public final static DeferredHolder<ElectricalPropertiesType<?>, ElectricalPropertiesType<ThreePhaseGeneratorProperties>>
		THREE_PHASE_GENERATOR = property("three_phase_generator",
			(pos, direction, d) -> new ThreePhaseGeneratorProperties(pos, direction));
	public final static DeferredHolder<ElectricalPropertiesType<?>, ElectricalPropertiesType<ConnectorProperties>>
		p02 = property("p02",
			(pos, direction, d) -> new ConnectorProperties(pos));
	public final static DeferredHolder<ElectricalPropertiesType<?>, ElectricalPropertiesType<ResistorProperties>>
		p03 = property("p03",
			(pos, direction, d) -> new ResistorProperties(pos, direction));
	public final static DeferredHolder<ElectricalPropertiesType<?>, ElectricalPropertiesType<CableProperties>>
		p04 = property("p04",
			(pos, direction, d) -> new CableProperties(pos, d));
	public final static DeferredHolder<ElectricalPropertiesType<?>, ElectricalPropertiesType<FourSlotAdapterProperties>>
		p05 = property("p05",
			(pos, direction, d) -> new FourSlotAdapterProperties(pos, direction));
	public final static DeferredHolder<ElectricalPropertiesType<?>, ElectricalPropertiesType<TwoSlotAdapterProperties>>
		p06 = property("p06",
			(pos, direction, d) -> new TwoSlotAdapterProperties(pos, direction));
	public final static DeferredHolder<ElectricalPropertiesType<?>, ElectricalPropertiesType<LightProperties>>
		p07 = property("p07",
			(pos, direction, d) -> new LightProperties(pos));
	public final static DeferredHolder<ElectricalPropertiesType<?>, ElectricalPropertiesType<ElectricMotorProperties>>
		p08 = property("p08",
			(pos, direction, d) -> new ElectricMotorProperties(pos, direction));
	public final static DeferredHolder<ElectricalPropertiesType<?>, ElectricalPropertiesType<TransformerProperties>>
		p09 = property("p09", (pos, direction, d) -> new TransformerProperties(pos, direction));
	public final static DeferredHolder<ElectricalPropertiesType<?>, ElectricalPropertiesType<CreativeGeneratorProperties>>
		p10 = property("p10", (pos, direction, d) -> new CreativeGeneratorProperties(pos));
	public final static DeferredHolder<ElectricalPropertiesType<?>, ElectricalPropertiesType<SwitchProperties>>
		p11 = property("p11", (pos, direction, d) -> new SwitchProperties(pos, direction));
	public final static DeferredHolder<ElectricalPropertiesType<?>, ElectricalPropertiesType<LargeTransformerProperties>>
		p12 = property("p12", (pos, direction, d) -> new LargeTransformerProperties(pos, direction));
	public final static DeferredHolder<ElectricalPropertiesType<?>, ElectricalPropertiesType<AccumulatorControlUnitProperties>>
		p13 = property("p13", (pos, direction, d) -> new AccumulatorControlUnitProperties(pos, direction));
	public final static DeferredHolder<ElectricalPropertiesType<?>, ElectricalPropertiesType<LargeSwitchProperties>>
		p14 = property("p14", (pos, direction, d) -> new LargeSwitchProperties(pos, direction));
	public final static DeferredHolder<ElectricalPropertiesType<?>, ElectricalPropertiesType<UnwirableConnectorProperties>>
		p15 = property("p15", (pos, direction, d) -> new UnwirableConnectorProperties(pos, d));
	
	public static <T extends ElectricalProperties> DeferredHolder<ElectricalPropertiesType<?>, ElectricalPropertiesType<T>> property (
		String name, ElectricalPropertiesType.PropertiesFactory<T> factory
	) {
		return PROPERTIES.register(name, id -> new ElectricalPropertiesType<>(id, factory));
	}
	
	public static void register(IEventBus bus) {
		PROPERTIES.register(bus);
	}
}
