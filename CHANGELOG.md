# Create: TFMG Community Edition 1.3.3
## Rant:

Contributors:<br>
@pouffy @ShallowAssumption

**Please note that not all bugs are fixed and some new additions are subject to change and should be considered experimental.**

## Changelog:
### Bug Fixes:
- Blast Furnace Outputs no-longer reset item components.
- Coke Ovens once again process faster the bigger they are.

### Changes:
- Recipe inputs now use tags where applicable.
- Added compositions for Crushed Lead & Nickel
- Fluid Compositions are now created under the `tfmg` namespace.
- Multimeters are now valid in the `belt` Curio slot.
- Engines:
  - Engines now drop all components and upgrades when destroyed.
  - Golden Turbo's speed and angle rendering now matches the Turbo's.
  - Added the `engine_upgrade` component to all default upgrade items.

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

### New Translations:
