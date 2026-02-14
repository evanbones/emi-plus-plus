# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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

