# Create: TFMG Community Edition 1.3.1
## Rant:
Ah yeah, hotfixes!


Contributors:<br>
@pouffy @wolfieboy09 @ShallowAssumption

**Please note that not all bugs are fixed and some new additions are subject to change and should be considered experimental.**

## Changelog:
### Bug Fixes:
- Coke oven multiblocks work again


### Changes:
- Light bulbs (& variants) allow more fine-tuned redstone control of their intensity.
- Cable Connectors print their current input mode on their multimeter tooltip.
- Fix Data Generation from failing when CLF: Reburned is not present

### API Changes:
- Added an optional `showIfEmpty` argument to `TFMGUtils.createFluidTooltip`, to reduce flashing on rapidly updating tanks.

### New Translations:
- Cable Connector Block
  - `block.tfmg.cable_connector.input_mode`
  - `block.tfmg.cable_connector.input_mode.true`
  - `block.tfmg.cable_connector.input_mode.false`
