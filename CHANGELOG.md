# Create: TFMG Community Edition 1.2.5
## Rant:
bleh

Contributors:<br>
@pouffy @wolfieboy09 @ShallowAssumption

**Please note that not all bugs are fixed and some new additions are subject to change and should be considered experimental.**

## Changelog:
### Bug Fixes:
- Concrete Hoses now render correctly.
- Blast Furnaces no longer crash the world with an index out of bounds.
- TFMG fluid handlers should allow pumps to interact again.

### Changes:
- Coke Oven:
  - Now hides progress when no recipe is being processed.
- Ponders:
  - Added ponders for the following:
    - Regular Engine Assembly
    - Radial Engine Assembly
- Vat:
  -  Recipes now have durations to prevent instant making of stuff, and some modified
  - `ChemicalVatCategory` now uses a dynamic barometer needle to display pressure.
  - Items now render inside vats
    - Also spins with mixing and the centrifuge in the direction of rotation.
  - Fluids no longer z-fight with the top of the vat.
- Oil Deposits:
  - Oil Reserves now drain over time.
  - Oil Reserves are now stored in a chunk attachment rather than level data.
  - Oil Reserves are now created on deposit generation instead of on pumpjack assembly.
  - Oil Deposit features now have a minimum height of 10 blocks.
  - Fossilstone in deposits now starts at half the height instead of 4.
  - Empty Oil Reserves now convert all contained deposits into Bedrock.
- Polarizer:
  - Displays tooltip through the multimeter, not goggles.
  - Tooltip rounds item charge (This helps prevent flickering).
  - Mechanical arms can now insert and extract items.
  - Particles now show when a recipe is completed.
- Blast Furnace:
  - Top Hatch is now evaluated with the rest of the multiblock.
  - Top Hatch now directly inserts items instead of dropping them.
  - Top hatch now properly saves its inventory.
  - Blast Furnace Hatches now limit certain inventories based on their place in the multiblock.
- Multimeters are no longer incompatible with Goggles

### API Changes:
- Created VAT operation registry.
- Marked `MachineConfig$polarizerItemChargingRate` as marked for removal.
- Created `TFMGClientConfig`
  - Created `UIConfig`.
- Vat:
  - Vats now use `VatOperation` for VAT machine IDs.
  - Vats now use `Pressure` instead of relying on an `int`.
  - `VatMachineRecipe$Builder` now has `pressure(int)` and `heatLevel(int)`.
  - `VatMachineRecipe` now uses correct builders.
  - `VatRecipeParams` now have default values (datagen kept throwing NPE).
  - Vat goggle information now shows recipe progress.
  - Fixed bug where recipe would get set to `null` causing recipes to take longer.
  - JEI rendering for Vat Operations, Vat Operation Tooltips & Vat Types is now handled through the `VatCategoryEvent` client event.
  - `drawVatTypes` & `drawSprites` in `ChemicalVatCategory` are now deprecated for removal.
- Electricity:
  - Removed `getPos()[long]` in `IElectric` in favour of `getPos()[BlockPos]`.
- Mixin `PipeAttachmentModelMixin#gatherModelData` no longer uses `@Overwrite`.
- Changed `TFMGRemapper` and `ComponentRemapper` a little.
- Polarizer:
  - Energy defaults to `2000` (2kW) 
  - Polarizing recipes now have `energy` as a field to specify required amount.
  - `PolarizingRecipe` now has an `energy` field.
  - Data gen can specify amount of energy.
  - Polarizers now have an output inventory.
  - `PolarizerBlockEntity#getItemChargingRate` now uses recipe charge rate.

### New Translations:

