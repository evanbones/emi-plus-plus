# EMI++

<a href='https://files.minecraftforge.net'><img alt="forge" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/forge_vector.svg"></a>
<a href='https://fabricmc.net'><img alt="fabric" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/fabric_vector.svg"></a>

**EMI++** is a Minecraft mod that acts as an extension to [EMI](https://github.com/emilyploszaj/emi), adding a variety
of useful features, improvements, and customization options to enhance the EMI experience.

## Features

EMI++ provides the following enhancements:

* **Stack Grouping:** Cleans up the EMI item list by grouping related items together (e.g., keeping all colored wools in
  one expandable entry).
    * *Includes built-in groups for:* Animal Armor, Banner Patterns, Copper Blocks, Infested Blocks, Minecarts, Pressure
      Plates, Spawn Eggs, and more.
* **Creative Mode Tabs:** Displays vanilla and modded Creative Mode tabs directly within the EMI interface for easy
  browsing.
* **Item Tabs:** Improved navigation with Creative Mode-style item tabs.
* **Vanilla/Modern Theme:** A unique visual theme for the sidebar, depending on the EMI theme.

### Dependencies

**Fabric:**

* Minecraft `~1.20.1`
* Fabric Loader `>=0.14.25`
* Fabric Language Kotlin `>=1.13.3+kotlin.2.1.21`
* [EMI](https://github.com/emilyploszaj/emi)

**Forge:**

* Minecraft `1.20.1` - `1.21`
* Kotlin for Forge `[4.10,)`
* [EMI](https://github.com/emilyploszaj/emi)

## Configuration

EMI++ offers extensive configuration options to tailor the interface to your needs. You can configure the mod via the
**in-game config screen** or by editing the configuration file directly.

### In-Game Config

1. Open the EMI overlay.
2. Click the **Config** (gear) icon.
3. Scroll down to the **EMI++** section.
4. From here you can toggle features and access sub-menus (such as the "Manage" button for Creative Mode Tabs).

### File Config

The configuration file is located at `config/emixx/emixx-common.toml`.

#### Creative Mode Tabs Settings

Controls the display and behavior of the creative tabs sidebar.

| Option                        | Type    | Default                   | Description                                                                                                          |
|:------------------------------|:--------|:--------------------------|:---------------------------------------------------------------------------------------------------------------------|
| `enableCreativeModeTabs`      | Boolean | `true`                    | Master switch to enable or disable the creative mode tab sidebar entirely.                                           |
| `syncSelectedCreativeModeTab` | Boolean | `true`                    | If enabled, clicking a tab in EMI++ will attempt to open that tab in the actual Creative Inventory screen (if open). |
| `disabledCreativeModeTabs`    | List    | `["minecraft:op_blocks"]` | A list of tabs that should be hidden from the EMI++ interface.                                                       |

#### Stack Groups Settings

Controls the item grouping behavior.

| Option              | Type    | Default | Description                                                                                                     |
|:--------------------|:--------|:--------|:----------------------------------------------------------------------------------------------------------------|
| `enableStackGroups` | Boolean | `true`  | Master switch to enable or disable stack grouping. If disabled, all items will appear individually in the list. |

## Customizing Stack Groups

You can define new custom stack groups or modify existing ones using **JSON files** (via Resource Packs) or **KubeJS**.

### Method 1: JSON Configuration (Resource Packs)

EMI++ loads stack groups from the `stack_groups` directory within the assets of the game (loaded via Resource Packs or
the config folder if configured).

To create a custom group, create a JSON file in `assets/<namespace>/stack_groups/my_group.json`.

**JSON Structure:**

| Field        | Type    | Description                                                                 |
|:-------------|:--------|:----------------------------------------------------------------------------|
| `id`         | String  | A unique identifier (e.g., `"mypack:currency"`).                            |
| `type`       | String  | Usually `"emixx:group"` for standard item lists.                            |
| `enabled`    | Boolean | Set to `false` to disable this group.                                       |
| `contents`   | List    | A list of items or tags to include.                                         |
| `exclusions` | List    | *(Optional)* Items to remove from the group (useful when using broad tags). |

**Example: Creating a shiny things group**

```json
{
  "id": "mypack:shiny_things",
  "type": "emixx:group",
  "contents": [
    "minecraft:diamond",
    "minecraft:emerald",
    "minecraft:gold_ingot",
    "#c:glass_blocks"
  ],
  "exclusions": [
    "minecraft:purple_stained_glass"
  ]
}

```

**Disabling Default Groups:**
To disable a default stack group (e.g., spawn eggs), you must override its definition using a Resource Pack. Create a
file with the same path/ID as the default group and set `"enabled": false`.

*Example: Disabling the spawn eggs group*

File: `assets/emixx/stack_groups/spawn_eggs.json`

```json
{
  "enabled": false
}

```

### Method 2: KubeJS

If you have KubeJS installed, you can register groups programmatically using the `EmiPlusPlusEvents` event group.

1. **Create a Script:** Place a script in your `kubejs/client_scripts/` folder.
2. **Register the Event:** Use `EmiPlusPlusEvents.registerGroups`.

**Example `kubejs/client_scripts/emi_groups.js`:**

```javascript
EmiPlusPlusEvents.registerGroups(event => {
    // Create a group from a Tag
    event.register("mypack:all_logs", "#minecraft:logs")

    // Create a group for a specific item
    event.register("mypack:command_blocks", "minecraft:command_block")
})
```

## License

[![Code license (MIT)](https://img.shields.io/badge/code%20license-MIT-green.svg?style=flat-square)](https://github.com/evanbones/emi-plus-plus/blob/main/LICENSE)