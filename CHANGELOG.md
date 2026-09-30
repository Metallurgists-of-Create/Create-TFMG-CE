# Create: TFMG Community Edition 1.3.2a
## Rant:
Ordering loads

Contributors:<br>
@pouffy

**Please note that not all bugs are fixed and some new additions are subject to change and should be considered experimental.**

## Changelog:
### Bug Fixes:
- Fixed load orders that stopped some recipe overrides from working.


### Changes:

### API Changes:
- new `IHaveMultimeterInformation` interface
  - `IElectric` now extends `IHaveMultimeterInformation` instead of `IHaveGoggleInformation`
  - Multimeters can now display information from any block entity that implements this interface

### New Translations:
