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


### Changes:
- Surface Scanner
  - Now can be picked up with shift and right-click of the wrench
- Flamethrower
  - Fuel type now pulls fluid lang key instead of relying on a new key

### API Changes:
- Removed `ILockablePipe`
- Removed `PipeAttachmentModelMixin`
- Pipe Locking now uses the `tfmg:locked_pipe` data attachment.


### New Translations:

