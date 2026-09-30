package com.drmangotea.tfmg.content.electricity.experimental;

import com.drmangotea.tfmg.content.electricity.experimental.simulation.nodes.ConnectableElectricalNode;

public class WireConnection {

    public final ConnectableElectricalNode node1;
    public final ConnectableElectricalNode node2;
    public final double resistance;
    public final boolean render;

    public WireConnection(ConnectableElectricalNode node1, ConnectableElectricalNode node2, double resistance,boolean render){
        this.node1 = node1;
        this.node2 = node2;
        this.resistance = resistance;
        this.render = render;
    }
}
