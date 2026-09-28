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
- Copycat Cable Blocks now function as actual copy cats.
- Fixed recipe duration on Blast Stoves being unreliable and improved recipe checking.


### Changes:
- Liquid Concrete now has it's dedicated colors for each dye type
- TFMG now uses the standard `90mB ⇒ 1 ingot` fluid units in recipes.
- Blast Stove
  - Capacity now scales with volume.
  - Maximum height changed to 5.
  - A modified Blast Stove will no-longer stop allowing pipe flow in some contexts.
- Surface Scanner
  - Scans 7x7 area rather than 5x5
  - Now can be picked up with shift and right-click of the wrench
  - Better visuals for rotating on sub-levels.
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
- Capacity:
  - Exhaust: 1000mB -> 4000mB
  - Smokestack: 8000mB -> 16000mB
- Exhaust Drain Rate:
  - Exhaust: 100mB/t -> 500mB/t
  - Flarestack: 25mB/t -> 100mB/t
  - Smokestack: 150mB/t -> 1000mB/t
- Smoke Timer:
  - Smokestack: 40t -> 500t
- The Engine Piping upgrade can now pull from any adjacent fluid handler instead of just Create's fluid tanks.
- Oil Deposits:
  - Fossilstone is now only placed where a stone-type block would be.
  - Oil Branches no-longer replace air.
  - Oil Deposits now properly generate at the very bottom of the world.
  - Oil Deposits will not place if the origin point isn't bedrock.

### API Changes:
- Removed `ILockablePipe`
- Removed `PipeAttachmentModelMixin`
- Pipe Locking now uses the `tfmg:locked_pipe` data attachment.
- Removed `ItemFluidTank`
- `FluidContainingItem`'s constructor now takes in a `Predicate<FluidStack>` as a validator instead of a strict `FluidEntry<?>`.


### New Translations:

