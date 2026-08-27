package com.drmangotea.tfmg.registry;

import com.drmangotea.tfmg.content.machinery.vat.electrode_holder.electrode.ArcElectrode;
import com.drmangotea.tfmg.content.machinery.vat.electrode_holder.electrode.Electrode;
import com.drmangotea.tfmg.content.machinery.vat.electrode_holder.electrode.ElectrodeEntry;

import static com.drmangotea.tfmg.TFMG.REGISTRATE;

public class TFMGElectrodes {
    public static final ElectrodeEntry<Electrode> NONE = REGISTRATE.electrode("none", Electrode::new)
            .properties((p) -> p)
            .register();

    public static final ElectrodeEntry<Electrode> COPPER = REGISTRATE.electrode("copper", Electrode::new)
            .properties((p) -> p.resistance(10).operationId(TFMGVatOperations.ELECTRODE))
            .register();

    public static final ElectrodeEntry<Electrode> ZINC = REGISTRATE.electrode("zinc", Electrode::new)
            .properties((p) -> p.resistance(10).operationId(TFMGVatOperations.ELECTRODE))
            .register();

    public static final ElectrodeEntry<ArcElectrode> GRAPHITE = REGISTRATE.electrode("graphite", ArcElectrode::new)
            .properties((p) -> p.resistance(300))
            .register();

    public static void init() { }
}
