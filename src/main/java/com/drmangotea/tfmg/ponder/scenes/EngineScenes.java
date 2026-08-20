package com.drmangotea.tfmg.ponder.scenes;

import com.drmangotea.tfmg.ponder.TFMGSceneBuilder;
import com.drmangotea.tfmg.registry.TFMGBlocks;
import com.drmangotea.tfmg.registry.TFMGItems;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class EngineScenes {

    public static void engines(SceneBuilder builder, SceneBuildingUtil util) {
        TFMGSceneBuilder scene = new TFMGSceneBuilder(builder);
        scene.title("engines", "Engines");
        scene.configureBasePlate(0, 0, 7);


        scene.showBasePlate();


        Selection engine = util.select().fromTo(3, 1, 3, 3, 1, 4);
        Selection engineFront = util.select().fromTo(3, 1, 2, 3, 1, 2);
        Selection engineFrontShaft = util.select().fromTo(2, 1, 2, 2, 1, 2);

        Selection lever = util.select().fromTo(4, 1, 2, 4, 1, 2);
        Selection cog = util.select().fromTo(3, 1, 0, 3, 1, 1);

        Selection fuelTank = util.select().fromTo(4, 1, 3, 5, 2, 3);
        Selection tank = util.select().fromTo(4, 1, 4, 4, 2, 4);
        Selection exhaust = util.select().fromTo(2, 1, 4, 1, 2, 4);
        scene.world().setKineticSpeed(fuelTank, 70);
        scene.world().setKineticSpeed(exhaust, 70);
        scene.world().setKineticSpeed(cog, 70);


        ElementLink<WorldSectionElement> engineElement = scene.world().showIndependentSection(engine, Direction.DOWN);
        ElementLink<WorldSectionElement> engineFrontElement = scene.world().showIndependentSection(engineFront, Direction.DOWN);

        scene.overlay().showText(50)
                .attachKeyFrame()
                .text("To build an engine, start by placing up to 5 engine blocks in a line");

        scene.idle(70);

        scene.overlay().showText(50)
                .attachKeyFrame()
                .text("The engine's tooltip will show you items needed for the assembly");
        scene.idle(60);

        Vec3 enginePos = util.vector().topOf(util.grid().at(3, 1, 2));
        scene.overlay().showControls(enginePos, Pointing.DOWN, 15)
                .rightClick()
                .withItem(new ItemStack(TFMGItems.CRANKSHAFT.get()));
        scene.idle(25);
        scene.overlay().showControls(enginePos, Pointing.DOWN, 15)
                .rightClick()
                .withItem(new ItemStack(TFMGBlocks.STEEL_COGWHEEL.get()));
        scene.idle(25);
        scene.overlay().showControls(enginePos, Pointing.DOWN, 15)
                .rightClick()
                .withItem(new ItemStack(TFMGBlocks.LARGE_STEEL_COGWHEEL.get()));
        scene.idle(40);

        scene.overlay().showControls(enginePos, Pointing.DOWN, 50)
                .rightClick()
                .withItem(new ItemStack(AllItems.EMPTY_SCHEMATIC.get()));


        scene.overlay().showText(50)
                .attachKeyFrame()
                .text("Engine configuration can be changed with a schematic");
        scene.idle(60);


        scene.overlay().showText(60)
                .attachKeyFrame()
                .text("Next step is inserting the cylinders (or turbine blades in turbine engines)");

        ElementLink<WorldSectionElement> engineFrontShaftElement = scene.world().showIndependentSection(engineFrontShaft, Direction.DOWN);
        scene.world().moveSection(engineFrontShaftElement, new Vec3(1d, -2d, 0d), 0);

        BlockPos pos = util.grid().at(3, 1, 2);
        for (int i = 0; i < 12; i++) {
            scene.idle(5);
            scene.tfmgInstructions().addPistonToEngine(pos);

            if (i == 3 || i == 7)
                pos = pos.south();
        }
        scene.idle(35);
        scene.world().moveSection(engineFrontShaftElement, new Vec3(0d, 2d, 0d), 0);
        scene.world().moveSection(engineFrontElement, new Vec3(0d, -2d, 0d), 0);

        scene.overlay().showText(50)
                .attachKeyFrame()
                .text("Shafts are inserted by right clicking");
        scene.overlay().showControls(enginePos, Pointing.DOWN, 50)
                .rightClick()
                .withItem(new ItemStack(AllBlocks.SHAFT));
        scene.idle(70);

        ElementLink<WorldSectionElement> fuelTankElement = scene.world().showIndependentSection(fuelTank, Direction.DOWN);
        scene.idle(10);
        ElementLink<WorldSectionElement> exhaustElement = scene.world().showIndependentSection(exhaust, Direction.DOWN);

        scene.overlay().showText(50)
                .attachKeyFrame()
                .text("Fuel input and exhaust output can be done from any block");
        scene.idle(70);

        scene.overlay().showText(50)
                .attachKeyFrame()

                .text("Every engine block can be right clicked with certain items to be upgraded");
        scene.idle(60);

        scene.overlay().showControls(util.vector().topOf(util.grid().at(3, 1, 4)), Pointing.DOWN, 40)
                .rightClick()
                .withItem(new ItemStack(TFMGBlocks.INDUSTRIAL_PIPE));

        scene.overlay().showText(70)
                .attachKeyFrame()
                .text("For example industrial pipes make the engine consume fuel from neighboring tanks");
        scene.idle(80);
        scene.world().showIndependentSection(tank, Direction.DOWN);
        scene.idle(30);
        scene.world().setKineticSpeed(engineFrontShaft, 70);
        scene.world().showIndependentSection(lever, Direction.DOWN);
        scene.world().showIndependentSection(cog, Direction.DOWN);
        scene.overlay().showText(128)
                .attachKeyFrame()
                .text("The engine can be started with a redstone signal");
    }

    public static void dieselEngine(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("diesel_engine", "");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();

        Selection engine = util.select().fromTo(2, 1, 0, 2, 1, 2);
        Selection exhaust = util.select().fromTo(2, 1, 3, 2, 2, 4);
        Selection air = util.select().fromTo(0, 1, 0, 1, 1, 2);
        Selection input = util.select().fromTo(3, 1, 0, 4, 1, 2);

        scene.idle(30);

        ElementLink<WorldSectionElement> engineElement = scene.world().showIndependentSection(engine, Direction.DOWN);
        scene.overlay().showText(50)
                .attachKeyFrame()
                .text("Diesel Engines are assembled by placing a shaft in the front of a diesel engine block");


        scene.idle(70);


        ElementLink<WorldSectionElement> inputElement = scene.world().showIndependentSection(input, Direction.DOWN);
        ElementLink<WorldSectionElement> exhaustElement = scene.world().showIndependentSection(exhaust, Direction.DOWN);
        scene.world().setKineticSpeed(input, 80);
        scene.world().setKineticSpeed(exhaust, 80);

        scene.overlay().showText(50)
                .attachKeyFrame()
                .text("Carbon Dioxide has to be outputted by pipes and exhaust block")
                .pointAt(util.vector().blockSurface(util.grid().at(2, 2, 4), Direction.WEST))
                .placeNearTarget();
        scene.idle(40);

        ElementLink<WorldSectionElement> airElement = scene.world().showIndependentSection(air, Direction.DOWN);
        scene.world().setKineticSpeed(air, 80);
        scene.overlay().showText(50)
                .attachKeyFrame()
                .text("Diesel engines need air that can be collected with air intakes")
                .pointAt(util.vector().blockSurface(util.grid().at(0, 1, 2), Direction.WEST))
                .placeNearTarget();
        scene.idle(10);
        scene.world().setKineticSpeed(engine, 128);
        scene.idle(70);
    }

    public static void dieselEngineExpansion(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("diesel_engine_expansion", "");
        scene.configureBasePlate(0, 0, 6);
        scene.showBasePlate();

        Selection engine = util.select().fromTo(2, 1, 0, 2, 1, 2);
        Selection input = util.select().fromTo(0, 1, 0, 1, 1, 2);
        Selection exhaust = util.select().fromTo(3, 1, 0, 4, 2, 2);

        Selection expansion = util.select().fromTo(2, 1, 3, 2, 1, 3);
        Selection air = util.select().fromTo(2, 1, 4, 2, 1, 5);
        Selection coolant = util.select().fromTo(0, 1, 3, 1, 1, 3);
        Selection oil = util.select().fromTo(3, 1, 3, 4, 1, 3);

        scene.world().setKineticSpeed(input, 80);
        scene.world().setKineticSpeed(exhaust, 80);
        scene.world().setKineticSpeed(air, 80);
        scene.world().setKineticSpeed(coolant, 80);
        scene.world().setKineticSpeed(oil, 80);
        scene.world().setKineticSpeed(engine, 128);


        scene.world().showIndependentSection(engine, Direction.DOWN);
        scene.world().showIndependentSection(input, Direction.DOWN);
        scene.world().showIndependentSection(exhaust, Direction.DOWN);
        ElementLink<WorldSectionElement> airElement = scene.world().showIndependentSection(air, Direction.DOWN);


        scene.world().moveSection(airElement, new Vec3(0d, 0d, -1d), 0);

        scene.idle(30);

        scene.world().moveSection(airElement, new Vec3(0d, 0d, 1d), 10);

        scene.idle(30);

        scene.world().showIndependentSection(expansion, Direction.DOWN);

        scene.idle(20);

        scene.world().showIndependentSection(coolant, Direction.DOWN);
        scene.world().showIndependentSection(oil, Direction.DOWN);

        scene.overlay().showText(100)
                .attachKeyFrame()
                .text("Diesel engine expansions give diesel engines 2 new fluid slots, for cooling and lubrication")
                .pointAt(util.vector().blockSurface(util.grid().at(2, 1, 3), Direction.WEST))
                .placeNearTarget();


        scene.idle(50);
    }

    public static void radialEngines(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("radial_engines", "");
        scene.configureBasePlate(0, 0, 5);
        scene.idle(10);

        scene.showBasePlate();


        Selection engineSmall = util.select().fromTo(2, 1, 1, 2, 1, 1);
        Selection engineLarge = util.select().fromTo(1, 1, 1, 1, 1, 1);
        Selection engineLever = util.select().fromTo(3, 1, 0, 3, 1, 0);


        Selection inputPump = util.select().fromTo(3, 1, 2, 3, 1, 2);
        Selection input = util.select().fromTo(3, 1, 1, 3, 1, 1);
        Selection tank1 = util.select().fromTo(3, 1, 3, 3, 2, 3);
        Selection tank2 = util.select().fromTo(2, 1, 3, 2, 2, 3);


        scene.world().setKineticSpeed(engineSmall, 0);


        ElementLink<WorldSectionElement> engineElement = scene.world().showIndependentSectionImmediately(engineSmall);
        scene.overlay().showText(50)
                .attachKeyFrame()
                .text("Radial Engines are a special Type of Engine that doesn't require an exhaust block")
                .pointAt(util.vector().blockSurface(util.grid().at(4, 0, 4), Direction.WEST))
                .placeNearTarget();
        scene.idle(100);


        scene.world().setKineticSpeed(inputPump, 80);
        ElementLink<WorldSectionElement> inputElement = scene.world().showIndependentSection(input, Direction.DOWN);
        scene.idle(50);

        BlockPos inputPos = util.grid().at(2, 1, 1);
        Vec3 topOf = util.vector().topOf(inputPos);
        scene.overlay().showControls(topOf, Pointing.DOWN, 20)
                .rightClick()
                .withItem(new ItemStack(AllItems.WRENCH.get()));

        scene.overlay().showText(70)
                .attachKeyFrame()
                .text("Clicking the Engine from one of its sides will spawn an input slot that can accept fuel and redstone signals")
                .pointAt(util.vector().blockSurface(util.grid().at(2, 1, 1), Direction.WEST))
                .placeNearTarget();
        scene.idle(100);
        scene.overlay().showText(40)
                .attachKeyFrame()
                .text("Regular Radial Engines uses gasoline as fuel")
                .pointAt(util.vector().blockSurface(util.grid().at(2, 1, 1), Direction.WEST))
                .placeNearTarget();


        scene.idle(80);


        ElementLink<WorldSectionElement> inputPumpElement = scene.world().showIndependentSection(inputPump, Direction.DOWN);
        ElementLink<WorldSectionElement> tankElement1 = scene.world().showIndependentSection(tank1, Direction.DOWN);
        ElementLink<WorldSectionElement> leverElement = scene.world().showIndependentSection(engineLever, Direction.DOWN);

        scene.world().setKineticSpeed(engineSmall, 180);
        scene.world().setKineticSpeed(engineLarge, 180);

        scene.overlay().showText(50)
                .attachKeyFrame()
                .text("Engine will start when redstone signal is applied to the input slot or the block itself")
                .pointAt(util.vector().blockSurface(util.grid().at(3, 1, 0), Direction.WEST))
                .placeNearTarget();

        scene.idle(100);

        scene.world().hideIndependentSection(engineElement, Direction.SOUTH);
        scene.world().hideIndependentSection(tankElement1, Direction.SOUTH);

        scene.idle(50);

        ElementLink<WorldSectionElement> largeEngineElement = scene.world().showIndependentSection(engineLarge, Direction.DOWN);
        ElementLink<WorldSectionElement> tankElement2 = scene.world().showIndependentSection(tank2, Direction.DOWN);
        scene.world().moveSection(largeEngineElement, new Vec3(1d, 0d, 0d), 0);
        scene.world().moveSection(tankElement2, new Vec3(1d, 0d, 0d), 0);

        scene.overlay().showText(50)
                .attachKeyFrame()
                .text("The second variant of a radial is The Large Radial Engine which uses kerosene as fuel");
        scene.idle(50);
    }
}
