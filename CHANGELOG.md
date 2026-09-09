# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [4.6.9] - 2026-09-08

### Fixed

- Fixed Fabric crashes.
- Improved compatibility with other mods that modify EMI.

## [4.6.8] - 2026-09-05

### Fixed

- Fixed startup crash.
- Fixed block tag tooltips displaying invisible blocks.

## [4.6.7] - 2026-09-04

### Changed

- Reverted 4.6.6 changes.

## [4.6.6] - 2026-09-03

### Fixed

- Fixed scrollbar thumb disappearing on tabs with too few items to scroll.

## [4.6.5] - 2026-09-03

### Changed

- Scrollbar thumb size is now clamped to a sensible minimum.

## [4.6.4] - 2026-08-31

### Fixed

- Fixed entity and block tags not working for in-game stackgroup creation.

## [4.6.3] - 2026-08-26

### Changed

- Updated translations (@CasualAnimalEnjoyer).

### Fixed

- Fixed creative inventory crash.
- Fixed stale stack group cache showing when searching creative tabs.
- Fixed searchbar clearing behaviour.

## [4.6.2] - 2026-08-19

### Fixed

- Various small fixes (@Kobber).

## [4.6.1] - 2026-08-18

### Fixed

- Fixed divide by zero crash.
- Fixed search bar text being invisible on first open.
- Fix certain config options resetting.

## [4.6.0] - 2026-08-18

**Note for Resource Pack creators:** Extensive changes have been made to tab textures, you will need to update your
pack.

### Changed

- **Major rework of Creative Mode Tabs**
    - Creative Tabs are now set to either "Horizontal" or "Vertical" and both now have **new textures** for both the
      transparent/modern and vanilla theme.
    - If all tabs are able to fit, the arrow buttons are now hidden to maximise available space.
    - Added **Tab Alignment** config: Start/Middle/End or Stretch-To-Fit (Stretching is the new default).
    - Added **Tab Width/Height** and **Icon Size** config: You can now fine tune the size of tabs. By default, icons are
      now 16px in both themes, the vertical tabs are a little smaller and the horizontal a little larger.
    - Improved tab and button placement and alignment, now properly accounting for things like vertical scrollbars and
      hidden headers.
    - Lots of small fixes and improvements.
- Minor tweaks to stack group textures.

### Fixed

- Fixed 1px misalignment in vertical scrollbar position and updated textures to compensate.
- Fixed search bar width not accounting for vertical scrollbar.

## [4.5.2] - 2026-08-17

### Fixed

- Fixed scrollbar visibility not updating when switching tabs.
- Fixed sidebar centering calculations with searchbar under index enabled.

## [4.5.1] - 2026-08-17

### Fixed

- Fixed EMI's sidebar not making room for the search bar, causing it to be pushed off screen on short windows.
- Fixed the vertical scrollbar's hitbox staying active on panels that have nothing to scroll.

## [4.5.0] - 2026-08-15

### Added

- Added a config option to disable the EMI global config styling.

### Fixed

- Fixed possible divide by 0 crash.

## [4.4.5] - 2026-08-14

### Changed

- Workstation stack groups now default to false.

### Fixed

- Performance improvements.

## [4.4.4] - 2026-08-14

### Fixed

- Fixed stale cache issue on first EMI load.

## [4.4.3] - 2026-08-14

### Added

- Added various config options for toggling search behavior (prefixes, searching by id).

## [4.4.2] - 2026-08-14

### Changed

- Previously hardcoded stack group textures are now resource-pack based (thanks, @Kobber!)

## [4.4.1] - 2026-08-13

### Fixed

- Fixed the search widget being oversized and misaligned the first time the inventory was opened after starting the
  game.

## [4.4.0] - 2026-08-12

### Added

- Added a config option for a vertical scrollbar (thanks, @Kobber!)

### Fixed

- Workstation tab performance improvements.

## [4.3.2] - 2026-08-12

### Changed

- Search bar behaviour now properly follows the EMI config.

## [4.3.1] - 2026-08-12

### Fixed

- Performance improvements.
- (Hopefully) fixed certain mods causing the index to break.
- Fixed stale stack group rendering in tabs other than the index.

## [4.3.0] - 2026-08-12

### Added

- Added support for additional workstations in the Workstation tab.
- Added support for stack groups and creative tab filtering in other tabs.
- Added additional config options for sidebar customization.

## [4.2.1] - 2026-08-11

### Added

- Added support for 2x2 crafting recipes in the Workstation tab when viewing the player inventory.

### Fixed

- Fixed a cache issue in the Workstation tab.

## [4.2.0] - 2026-08-11

### Added

- Added a Workstation tab!
    - This can be used to filter by valid recipes in the currently open workstation.
- Added support for regex in fluid/effect matching.

### Changed

- Adjusted default search widget offset when aligned under the index tab.
- Improved stack group parsing to be more lenient.

### Fixed

- Fixed issues with JEED.

## [4.1.1] - 2026-08-07

### Fixed

- Fixed issues with EMI craftables.

## [4.1.0] - 2026-08-06

### Added

- Added Russian translation (@CasualAnimalEnjoyer).
- Added additional config options for customizing the search bar.

### Fixed

- Fixed Creative Tabs being rendered outside the screen (@crococrystal).
- Fixed issues with the align with panel search bar option.
- Adjusted header centering on small window sizes.

## [4.0.3] - 2026-08-05

### Fixed

- Fixed issues with Better Cheat Mode.

## [4.0.2] - 2026-08-02

### Added

- Added a config option to disable REMI's search box changes.

## [4.0.1] - 2026-07-30

### Changed

- The toggled state of the recipe book is persisted when EMI Only In Recipe Book is enabled.

### Fixed

- Fixed tags in recipes being unclickable.

## [4.0.0] - 2026-07-30

### Changed

- Renamed mod from EMI++ to Reliable EMI.
- Changed mod ID from `emixx` to `remi`.
    - Existing configs should still be compatible, though I'd recommend migrating your configs to the `remi` namespace.

### Added

- Added an improved cheat mode config option (default: enabled).
    - When enabled and in cheat mode, items can be directly dragged into your inventory from EMI.

### Fixed

- Fixed gaps in block tag displays.
- Fixed Fabric crash.
- Fixed issues with the colored search bar text config option.

## [3.5.1] - 2026-07-28

### Fixed

- Fixed some config options not loading properly.
- Fixed `exclusions` being ignored on `emixx:regex` stack groups.
- Fixed other bugs with the `emixx:regex` stack group type.

## [3.5.0] - 2026-07-26

### Added

- EMI's tag tabs are now split into dedicated category tabs for Item Tags, Block Tags, Fluid Tags, and Entity Tags.
- Entity type tags are now registered in EMI and displayed when right-clicking spawn eggs in the index.
- Searching with the `#` tag prefix or `r#` now supports searching entity type tags and fluid tags (e.g.,
  `#skeletons`).
- Added support for creating and toggling stack groups based on block tags directly from tag recipe pages.
- Added config options for the new tag tabs.

### Fixed

- Fixed an EMI bug where the block tags tab was missing due to unregistered block adapters.

## [3.4.0] - 2026-07-26

### Added

- Added new config options for the search widget:
    - **Horizontal/vertical padding**
    - **Vertical offset**
    - **Align with bottom of panel**
    - **Text color**
- Added a config option to change the number of Creative tabs displayed.

### Changed

- The search widget now uses its own unique texture.

## [3.3.2] - 2026-07-25

### Fixed

- Fixed edge case stack group matching.

## [3.3.1] - 2026-07-25

### Fixed

- Fixed certain tags not matching in stack groups.

## [3.3.0] - 2026-07-25

### Added

- Added new config options:
    - **Disable Pagination Wrapping**: Disables wrapping around to the first/last page.
    - **Enable Scroll Mode**: Disable pages and scroll through items one row at a time instead.
    - **Show Page Title in Headers**: Hides page numbers from headers and shows the title of the page instead. If using
      creative tabs, the active creative tab title is used for the Index page.
    - **Hide Single Sidebar Switch Button**: Hides the sidebar page switching button if there's only one page in the
      sidebar.
    - **Incremental Scrollbar Fill**: Fills the sidebar scrollbar from left to right as you scroll or page through,
      instead of showing a discrete chunk (RRV fans wya?).
- Added a warning that stack group modification is only possible while inside a world.

### Changed

- Switched to YACL instead of modifying EMI's config screen.

### Fixed

- Fixed tabs not displaying under certain conditions.
- Fixed desync between enabled/disabled stack groups in the config screen and the tag button.

## [3.2.1] - 2026-07-19

### Fixed

- Fixed tooltip issues with Shadows Redropped.

## [3.2.0] - 2026-07-19

### Fixed

- Fixed `priority` field only applying to `emixx:group` types.
- Performance improvements.

## [3.1.2] - 2026-07-06

### Changed

- Added an up arrow to the creative tabs list when using vanilla mode.

## [3.1.1] - 2026-07-02

### Fixed

- Improved automatic tag translations.

## [3.1.0] - 2026-06-19

### Added

- Added an optional `"priority"` field to stack group configurations (default: 0).
- Exclusions, tags, regex, and content rules can now be combined.
- You can now search for stack groups by name/ID directly in EMI.
    - The previous method of using `%` still works.

### Fixed

- Fixed issues with custom Recreative icons.

## [3.0.0] - 2026-06-18

### Added

- Added a config page to quickly disable any built-in (or added) stack groups.
- Items inside stack groups can now be rearranged in the config screen, or through the `stackGroupItemOrder` config
  option.

### Changed

- Rewrote the mod using Java instead of Kotlin.
- Fabric Language Kotlin and Kotlin for Forge are no longer dependencies.

### Fixed

- Fixed issues with Create Simulated tabs.
- Performance improvements.

## [2.3.0] - 2026-06-18

### Added

- Added support for defining groups using regex.

## [2.2.0] - 2026-06-06

### Fixed

- Fixed items inside EMI++ stack groups not being able to be favourited.

## [2.1.3] - 2026-05-30

### Changed

- Updated Chinese translations (@LLLCYL).

### Fixed

- Fixed strange tooltip rendering while using the Vanilla theme.

## [2.1.2] - 2026-05-26

### Added

- Added support for arbitrary `ResourceLocation`s in the creative tabs when using Recreative.

## [2.1.1] - 2026-05-25

### Fixed

- Fixed GUI issues with Mekanism.

## [2.1.0] - 2026-05-04

### Added

- Added a config option to only have EMI open when the recipe book is open.

### Changed

- Slightly tweaked vanilla-style Creative tabs.

### Fixed

- Fixed top Creative tab missing a single pixel (literally unplayable).

## [2.0.3] - 2026-05-04

### Fixed

- Fixed missing lang entries for new config option.
- Fixed missing chiseled copper blocks in the copper block stackgroup.

## [2.0.2] - 2026-05-04

### Added

- Added config option to disable the creative tab display.

### Fixed

- Fixed missing copper blocks in the `copper_blocks` stackgroup.

## [2.0.1] - 2026-05-03

### Fixed

- Fixed incompatibility with Shadow Drop.
- Fixed slightly cramped Creative tabs when in Vanilla theme.

## [2.0.0] - 2026-05-03

- Ported to 1.21.1.

## [1.4.0] - 2026-04-16

### Added

- Added support for synthetic entries like fluids and effects.
- Added an optional field for custom translations for stack group names.
- Added support for custom tabs from Recreative.

## [1.3.3] - 2026-03-24

### Added

- Added support for subfolders in stack group configs.

## [1.3.2] - 2026-03-13

### Added

- Added config option to disable stack group button.

## [1.3.1] - 2026-02-28

### Fixed

- Improved KubeJS integration.

## [1.3.0] - 2026-02-23

### Fixed

- Improved performance while searching.
- Reworked NBT matching again.

## [1.2.12] - 2026-02-18

### Fixed

- Fixed issue where stacks with NBT would be re-grouped.

## [1.2.11] - 2026-02-15

### Added

- Added tooltips to the Creative Mode style EMI tabs.

### Changed

- Improved dynamic stack group searching logic.

## [1.2.10] - 2026-02-14

### Fixed

- Hidden items in EMI are now also hidden in stack groups.

## [1.2.9] - 2026-02-14

### Changed

- Searching for stack groups in EMI now requires a `%` prefix operator.

## [1.2.8] - 2026-02-12

### Fixed

- Fixed rare EMI rendering error.
- Fixed certain items with NBT not displaying their proper NBT.

## [1.2.7] - 2026-02-10

### Added

- Stack group names now show up in EMI search results.

### Fixed

- Fixed highlights persisting in the EMI menu.
- Fixed stackgroups not recursively populating from item tags.

## [1.2.6] - 2026-02-09

### Fixed

- Improved memory usage with large stackgroups.

## [1.2.5] - 2026-02-08

### Fixed

- Performance improvements when baking many stack groups.

## [1.2.4] - 2026-02-07

### Fixed

- Further performance improvements when searching.
- Fixed possible sidebar-related rendering crash.

## [1.2.3] - 2026-02-04

### Fixed

- Fixed lag inside Tinker's anvils.
- Fixed lag when using many stack groups.

## [1.2.2] - 2026-02-03

## Added

- Reimplemented stack group button toggle.
- Automatic name parsing for manual stack group additions.

### Fixed

- Hopefully fixed Mekanism compat.

## [1.2.1] - 2026-02-03

### Fixed

- Reworked JSON serialization.

## [1.2.0] - 2026-02-02

### Changed

- Switch to resource pack configuration for stack groups.
- Large backend cleanups.

### Fixed

- Mekanism-related crash on Forge.

