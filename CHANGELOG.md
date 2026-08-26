# Create: TFMG Community Edition 1.2.4c
## Rant:
bleh

Contributors:<br>
@pouffy @wolfieboy09 @ShallowAssumption @pepagg

**Please note that not all bugs are fixed and some new additions are subject to change and should be considered experimental.**

## Changelog:
### Bug Fixes:

### Changes:
- Coke Oven
  - Now hides progress when no recipe is being processed
- Ponders
  - Added ponders for the following:
    - Regular Engine Assembly
    - Turbine Engine Assembly
    - Radial Engine Assembly
- Vat
  - `ChemicalVatCategory` now uses a dynamic barometer needle to display pressure

### API Changes:
- Created VAT operation registry
- Created `TFMGClientConfig`
  - Created `UIConfig`
- Vat
  - Vats now use `VatOperation` for VAT machine IDs
  - Vats now use `Pressure` instead of relying on an `int`
  - `VatMachineRecipe$Builder` now has `pressure(int)` and `heatLevel(int)`


### New Translations:

