# Create: TFMG Community Edition 1.3.3
## Rant:

Contributors:<br>
@pouffy @ShallowAssumption

**Please note that not all bugs are fixed and some new additions are subject to change and should be considered experimental.**

## Changelog:
### Bug Fixes:
- Blast Furnace Outputs no-longer reset item components.

### Changes:
- Recipe inputs now use tags where applicable.
- Added compositions for Crushed Lead & Nickel
- Fluid Compositions are now created under the `tfmg` namespace.

### API Changes:
- new `IHaveMultimeterInformation` interface
  - `IElectric` now extends `IHaveMultimeterInformation` instead of `IHaveGoggleInformation`
  - Multimeters can now display information from any block entity that implements this interface

### New Translations:
