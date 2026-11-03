# Create: TFMG Community Edition 1.3.3
## Rant:
Heh, it's been a while.

I've had the art department hard at work replacing a bunch of textures that needed replacing.<br>
They've done a great job, and we hope you like some of the new textures!

This update also comes with some more customisability options and ways for mod/pack devs to expand upon our systems!<br>
Oh yeah, and some bug fixes. That's probably what you're here for isn't it.

**Please note that not all bugs are fixed and some new additions are subject to change and should be considered experimental.**

## Contributors:
### Developers:
- @pouffy
- @ShallowAssumption
- @wolfieboy09
### Artists:
- @Nerf-Ashton
- @ImmanisVir
- @YYY000YYY
- @DragonVenom360


## Changelog:
### Bug Fixes:
- Blast Furnace Outputs no-longer reset item components.
- Coke Ovens once again process faster the bigger they are.
- Concrete-logged Electric Posts no longer dry into Concrete-encased cables
- Engines and fireboxes no-longer exhaust `flowing_carbon_dioxide` instead of `carbon_dioxide`.
- Fixed an issue where double slabs only dropped one slab.
- Fixed a crash related to thermite grenades.

### Changes:
- Recipe inputs now use tags where applicable.
- Added compositions for Crushed Lead & Nickel
- Fluid Compositions are now created under the `tfmg` namespace.
- Multimeters are now valid in the `belt` Curio slot.
- Diagonal Cables can now be horizontal.
- Firebox's fuel tank can now be extracted from.
- Engines:
  - Engines now drop all components and upgrades when destroyed.
  - Golden Turbo's speed and angle rendering now matches the Turbo's.
  - Added the `engine_upgrade` component to all default upgrade items.
  - Simple Large Engines now have their own fuel tag.
  - Slightly tweaked engine fuel testing to hopefully avoid crashing on a broken tag.
- Blast Furnace Output:
  - Tanks now use `ForceableFluidTank` and block insertion.
  - Now uses the same timer system as all our other recipe machines.
  - Tweaked recipe durations.
  - Smoke particles are no-longer created if a top hatch is present.
- JEI recipe categories now show TFMG instead of Create.
- Oil Wells & Deposits now have proper feature configs.
- All fluids are now properly tagged.
- Create: The Factory Must WORK! 2.1.2 is no longer marked as incompatible.
- A load of textures have been reworked.

### API Changes:
- new `IHaveMultimeterInformation` interface
  - `IElectric` now extends `IHaveMultimeterInformation` instead of `IHaveGoggleInformation`
  - Multimeters can now display information from any block entity that implements this interface
- Vat Operations
  - Renamed `tfmg:mixing` to `tfmg:mixer` & `TFMGVatOperations#MIXING` to `TFMGVatOperations#MIXER`
  - Renamed `tfmg:freezing` to `tfmg:freezer` & `TFMGVatOperations#FREEZING` to `TFMGVatOperations#FREEZER`
- Added `tfmg:engine_upgrade` data component.
- Improved registry creation.
- Added tag generator for Engine Upgrades.
- Engine Upgrades are now a registry.
- Electricity generation on engine upgrades is now an interface.
- All engines now have registered item handlers.
- Engine Fuels can now specify custom exhaust fluids (CO2 remains the default).

### New Translations:
