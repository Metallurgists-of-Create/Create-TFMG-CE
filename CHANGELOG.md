# Create: TFMG Community Edition 1.3.2
## Rant:


Contributors:<br>
@pouffy @wolfieboy09 @ShallowAssumption

**Please note that not all bugs are fixed and some new additions are subject to change and should be considered experimental.**

## Changelog:
### Bug Fixes:
- Fix rare `ConcurrentModificationException` being thrown with `ElectricalNetwork#checkForLoops`
- TFMGTiers now uses proper mineable tags.
- Rebar recipe now produces 2 per ingot instead of 4 to prevent duping.
- Cooling Fluid Bottles and Oil Cans no-longer void their contents instantly.
- Blast Furnace top hatches no-longer void items.


### Changes:
- TFMG now uses the standard `90mB ⇒ 1 ingot` fluid units in recipes.
- Blast Stove
  - capacity now scales with volume
  - maximum height changed to 5
- Surface Scanner
  - Now can be picked up with shift and right-click of the wrench
- Flamethrower
  - Fuel type now pulls fluid lang key instead of relying on a new key
- `Rutile` Integration
  - Added chemical composition files for relevant items/fluids.
- `Create: Big Cannons` Integration:
  - Removed mixer alloying recipes.
  - Removed compacting-casting recipes.
  - Added Melting recipes for TFMG metals.
  - Added Casting Basin recipes for CBC metals.
  - Added Vat recipe for Nethersteel.
- Chemical Vat
  - Vat recipes in JEI now cycle all valid vat types.
- Distillation Tower
  - Distillation Controller uses recipe-specific durations.
  - Timer progression is based on the heat level of the tower.
  - Progress percentage now displays in the goggle tooltip.
  - Tweaked durations for all distillation recipes (200 ticks per output).

### API Changes:
- Removed `ILockablePipe`
- Removed `PipeAttachmentModelMixin`
- Pipe Locking now uses the `tfmg:locked_pipe` data attachment.
- Removed `ItemFluidTank`
- `FluidContainingItem`'s constructor now takes in a `Predicate<FluidStack>` as a validator instead of a strict `FluidEntry<?>`.


### New Translations:

