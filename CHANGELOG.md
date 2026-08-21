# Create: TFMG Community Edition 1.2.4
## Rant:
Yippee new version! <br>
This one has quite a few changes to how some parts of the mod work, but it is all safely converted so don't worry!!

I can't be bothered to note all the contributors after each change, so I'll just dump them here:<br>
@pouffy @ShallowAssumption @wolfieboy09

And now for the notice at the start of every changelog!<br>
Please note that not all bugs are fixed and some new additions are subject to change and should be considered experimental.

## Changelog:
### Bug Fixes:
- Our version checker no-longer prints html.
- Re-implemented the Vat Evaluation packet so vats should now evaluate properly on clients.
- All TFMG packets are now properly registered under `tfmg` instead of `create`.
- Updated `doesntWorkWith` in IndustrialMixerBlockEntity to use the namespaced electrode ids.
- Configuration Wrench now only returns a successful interact if the player is crouching.
- Fixed Flarestack violently flashing during low input speed.
- Fixed Blue Sparks not creating fire on hit.
- Air Intake can now fill up to their capacity when production is high.
- Coke Ovens facing each-other no longer freeze.
- Polarizer charge percentage now resets regardless of power usage.
- Pumpjack disassembly now has a small delay to stop them disassembling on a quick chunk reload.
- Various Item containers now properly drop their inventories when destroyed.
- Steel Tanks now passively update their boiler state.
- Traffic Lights can now be green.
- Fixed two instances of components being retrieved unsafely in the Winding Machine.
- Lightning strikes now properly perform polarizing recipes.
- Cleared up TFMG's JEI plugin and removed Create's copied over implementation.
- Engine Cylinders and Turbine Blades have recipes again.
- Engines of different types no longer connect.
- Encased cogwheel item models now show the correct cogwheel type.
- Steel, Aluminium & Heavy Casings can now encase blocks properly.
- Fixed typos in some cogwheel texture names.
- Segmented Displays now safely decode their display contents.
- Rolled back a change done to `IElectric` that made Power Grid incompatible.
- Industrial Mixers should no-longer void the Mixer Blade/Centrifuge item.
- Electrode Holders should no-longer void the Electrode item.
- Added native support for Chemica.
- Engines are now mineable.
- Spools properly prevent duplicate connections.
- Cable Insulators deduplicate existing connections on-load.

### Changes:
- Added some new configs:
  - Compressor minimum RPM.
  - Industrial Mixer minimum RPM.
  - Surface Scanner minimum RPM.
  - Freezer minimum current.
  - Polarizer minimum power.
- Added more config groups to the machine config.
- Blast Furnace:
  - Multiblock logic has been separated into a new class.
  - Tuyere position and multiblock size are now saved to the block entity nbt.
  - Multiblock evaluation happens every lazyTick instead of once on placement.
  - Added `lazyTick` to Blast Stoves to update connectivity.
- Vat:
  - Vat Machines now display personal goggle/multimeter info if they are operational or not.
  - Vats can now be multi-placed.
  - Vat pressure and heat level do not display floats any more.
  - Vat recipe `allowed_vat_types` now uses a list of `ResourceLocation`s.
  - Vat types now use `ResourceLocation` in place of `String`.
  - `VatRecipeGen` no longer uses generics.
  - Vat pressure is limited to between `-9` and `9` (inclusive).
  - Industrial Mixers now have an item handler.
  - Added `tfmg:mixer_mode` registry.
  - Added `tfmg:mixer_mode` data component.
  - `IndustrialMixerBlock` now handles item insertion and extraction better.
  - Mixer rendering is now handled through the registered mode.
  - Industrial Mixers now also update the vat during a speed change or when their item handler is changed.
  - The Vat's item inventory now notifies the vat when it is updated.
  - Multiple vat attachments of the same type are now grouped in the tooltip.
  - Compressors now have operation ids: `tfmg:compressor` and `tfmg:decompressor`.
  - Freezers now have an operation id: `tfmg:freezer`.
  - Compressors now notify the vat when their speed changes.
- Electricity:
  - Modified the extraction calculation on Cable Insulators to improve the amount of FE being consumed.
  - Nicer Cable visuals.
- Engines & Engine Adjacent:
  - Improved Air Intake goggle tooltip.
  - Engine types are now a registry.
  - Correct cylinders are now tag-based. 
    - Regular Engine denies `tfmg:engine/turbine`. 
    - Turbine Engine denies `tfmg:engine/cylinder`.
    - Both cases require the input to have the `tfmg:engine_cylinder` component to be valid.
  - Engine types are safely remapped from their legacy values when reading from engine nbt.
  - Added JEI search aliases for Turbine Blades (`turbine`) & Engine Cylinders (`piston`).
- Surface Scanners:
    - Surface Scanner grid now accuratley based on chunk grid
    - Surface Scanners on Sub-Levels now re-scan when moved into a new chunk.
    - Surface Scanners now produce a redstone signal aiming towards the nearest oil chunk.
- Added TFMG Encased wooden cogwheels.
- Added Industrial Aluminium encased blocks.
- Cleaned up Neon Tube.
- Marked "The Factory Must WORK" as incompatible with Community Edition due to conflicting bug fixes. This may be revoked at a later date.
- Added credits to `neoforge.mods.toml`.
- Coke Oven progress is now displayed as a percentage.
- Winding Machine:
  - Change spool to `SpoolInventory`
  - Support legacy `Spool` NBT and migrate to new system
  - Create an inventory for output
  - Shift-right click now takes out the spool instead of the item
    - Normal right click now takes out the item and resulting item
  - Wire stops rendering when a recipe has been completed
  - Fixed spool not being saved/loaded correctly (`read()` parsed spool but never assigned it)
  - Deployers can insert spools and items into the winding machine
    - Deployers can also take out empty spools
    - Prevent deployers from taking out non-empty spools
  - Empty spool can be emptied from the spool side
  - Prevent items from being inserted when output is full

### API Changes:
- Added Sable Companion as an embedded api.
- Updated mixin compatibility level. (JAVA 8 → JAVA 21)
- JEI integration has been moved to a new folder. (`recipes` → `inregration.jei`)
- Engine types are now blacklisted from the schematic cycle via the `tfmg:schematic_cycle_blacklist` tag.
- Engine Upgrades now use the `tfmg:upgrades_on_side` engine tag for rendering.
- Large Engine fuels are now determined through the `tfmg:large_engine` engine fuel type tag.
- Removed `RegularEngineBlockEntity.EngineType` enum.
- Internal tag enums in `TFMGTags` have been renamed (`TFMGTags.TFMGItemTags` → `TFMGTags.Items`).
- Added `formatFluid` in `TFMGUtils` for fluid units.
- Added `fluidProduction` in `TFMGTexts` for fluid production.
- Added `count` in `TFMGTexts.Vat` for Vat Attachment counts.
- Renamed & remapped `heavy_casing_encased_` blocks to `heavy_encased_`.
- Added Decimal Formats to `TFMGTexts` for properly formatting numbers.
- Re-introduced Micron unit just in case.
- Added `returnItemToInventory` method to `TFMGUtils`.
- Removed `IndustrialMixerBlockEntity.MixerMode` enum.
- Removed `TFMGLang.temporaryText`.
- Removed `IHaveCables`.
- Flamethrower Fuels now use the `tfmg:hellfire` tag instead of the `hellfire` boolean.
- Flamethrower Fuels now use the `tfmg:cold` tag instead of the `is_cold` boolean.
- Removed `"hellfire"` and `"is_cold"` from `FlamethrowerFuelType` in favour of tags.
- Added `rotateQuat` method to `TFMGUtils`.
- Item component remapping is now handled via `ComponentRemapper` instead of the item class.
- Moved TFMGRemapper from `base` → `remap`.
- The following are deprecated for removal. If you are migrating your addon to depend on Community Edition I'd recommend fixing these:
  - `IElectric.getPos()` (returns long). We are trying to move away from storing BlockPos as a long and you should use `IElectric.position()` instead.
  - `Electrode.Properties.item(ItemEntry<?>)`. Electrodes are stored as a data component and this is now irrelevant. You should assign a default component to your electrode item instead.
  - `Electrode.Properties.operationId(String)` The operationId in Electrodes is now a ResourceLocation. You should use the `operationId(ResourceLocation)` builder method instead.
  - `FuelType` & `FuelType.Builder` are no-longer used to register custom engine fuels. Instead, use a datapack registry with `TFMGRegistries.ENGINE_FUEL_TYPE` to create types.
  - `TFMGDataComponents.FLAMETHROWER_FUEL` is only used for remapping and **will** be removed at a later date.
  - Engine Cylinders are now created with the `TFMGDataComponents.ENGINE_CYLINDER` component. Consequently, the following **will** be removed at a later date:
    - `TFMGDataComponents.FUEL_TAGS`, no longer used for anything.
    - `TFMGDataComponents.FUELS`, only used for remapping.
    - `CylinderItem`, only exists for addons that still use it.

### New Translations:
People who wish to translate this mod should look out for changes here.
- For new and removed translations check the latest en_us.json in the `src/generated/assets/tfmg/lang` folder.
- The following already exist but will need changing:
  - "tfmg.goggles.vat.tfmg.centrifuge" | "&nbsp;&nbsp;&nbsp;Centrifuge" → "Centrifuge"
  - "tfmg.goggles.vat.tfmg.electrode" | "&nbsp;&nbsp;&nbsp;Electrode" → "Electrode"
  - "tfmg.goggles.vat.tfmg.graphite_electrode" | "&nbsp;&nbsp;&nbsp;Graphite Electrode" → "Graphite Electrode"
  - "tfmg.goggles.vat.tfmg.mixing" | "&nbsp;&nbsp;&nbsp;Mixer" → "Mixer" 
  - "tfmg.multimeter.large_transformer.air_cooled" | "&nbsp;&nbsp;&nbsp;State: Air Cooled" → "Air Cooled"
  - "tfmg.multimeter.large_transformer.metal_cooled" | "&nbsp;&nbsp;&nbsp;State: Metal Heat Sink Cooled" → "Metal Heat Sink Cooled"
  - "tfmg.multimeter.large_transformer.oil_cooled" | "&nbsp;&nbsp;&nbsp;State: Oil + Heat Sink Cooled" → "Oil + Heat Sink Cooled"
