# EMI++

**EMI++** is a Minecraft mod that acts as an extension to [EMI](https://github.com/emilyploszaj/emi), adding a variety of useful features, improvements, and customization options to enhance EMI.

## Features

EMI++ provides the following enhancements:

* **Stack Grouping:** cleans up the EMI item list by grouping related items together.
  * *Includes groups for:* Animal Armor, Banner Patterns, Copper Blocks, Infested Blocks, Minecarts, Pressure Plates, Spawn Eggs, and more.

* **Creative Mode Tabs:** Displays Creative Mode tabs within the EMI interface.
* **Item Tabs:** Improved navigation with item tabs.

## Installation

EMI++ is available for both **Fabric** and **Forge**.

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

EMI++ offers basic configuration options. You can access the configuration screen through the standard mod menu config integration or in-game EMI settings.

The compiled jar files will be located in the `fabric/build/libs` and `forge/build/libs` directories.

## Custom Stack Groups

You can define new custom stack groups using either **JSON configuration files** or **KubeJS scripts**.

### Method 1: JSON Configuration

This is the standard method for adding stack groups without additional mods.

1. **Locate the Config Folder:**
    Navigate to your Minecraft instance's config folder and find the directory:
   `config/emixx/groups/`
   *(If the `groups` folder doesn't exist, create it).*
2. **Create a JSON File:**
   Create a new `.json` file in this folder (e.g., `my_custom_group.json`). The filename does not strictly matter, but it is good practice to match it to your group ID.
3. **Define the Group:**
   The JSON object must contain an `id` and a list of `contents`. 
* **id**: A unique string identifier (e.g., `"mypack:currency"`).
* **contents**: A list of ingredients to include. These can be item IDs (e.g., `"minecraft:diamond"`) or tags (e.g., `"#minecraft:logs"`).
* **exclusions** *(optional)*: A list of ingredients to remove from the group if they were included by the `contents` (useful when using broad tags).


**Example `config/emixx/groups/shiny_things.json`:**
```json
{
  "id": "mypack:shiny_things",
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

### Method 2: KubeJS

If you have KubeJS installed, you can register groups programmatically.

1. **Create a Script:**
   Create a script file in your `kubejs/client_scripts/` folder (e.g., `emi_groups.js`).
2. **Register the Event:**
   Use the `EmiPlusPlusEvents.registerGroups` event.
3. **Add Groups:**
   Call `event.register("id", ingredient)` to add a group. The `ingredient` can be an item ID, a tag, or other valid KubeJS ingredient parsables.
   
**Example `kubejs/client_scripts/emi_groups.js`:**
```javascript
EmiPlusPlusEvents.registerGroups(event => {
    // Create a group from a Tag
    event.register("mypack:all_logs", "#minecraft:logs")

    // Create a group for a specific item
    event.register("mypack:command_blocks", "minecraft:command_block")
})
```

### Notes

* You may need to restart the game or run `/reload` to see changes.
* You can disable specific groups (default or custom) in the main `emixx-common.toml` config file under `disabledStackGroups`.

## License

This project is licensed under the **MIT License**.
