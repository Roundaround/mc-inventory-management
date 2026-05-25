![Inventory Management](https://i.imgur.com/wXZra91.png)

![](https://img.shields.io/badge/Loader-Fabric%20|%20Forge%20|%20NeoForge-313e51?style=for-the-badge)
![](https://img.shields.io/badge/MC-26.1--26.1.2%20|%201.21%20|%201.20%20|%201.19%20|%201.18.2-313e51?style=for-the-badge)
![](https://img.shields.io/badge/Side-Client%20+%20Server-313e51?style=for-the-badge)

[![Modrinth Downloads](https://img.shields.io/modrinth/dt/inventory-management?style=flat&logo=modrinth&color=00AF5C)](https://modrinth.com/mod/inventory-management)
[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1293402?style=flat&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/inventory-management)
[![GitHub Repo stars](https://img.shields.io/github/stars/Roundaround/mc-fabric-inventory-management?style=flat&logo=github)](https://github.com/Roundaround/mc-fabric-inventory-management)

[![Support me on Ko-fi](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/donate/kofi-singular-alt_vector.svg)](https://ko-fi.com/roundaround)

Sort and transfer items with the click of a button. Adds several buttons to your inventory and containers that let you sort, transfer, and automatically stack your items! Works in single player by installing on your client, and in multiplayer by installing on both your client AND the server.

![](https://i.imgur.com/GadzOcM.png)

---

### Configuration

_* Configuration capabilities added in 1.1.0 for 1.19. The version for 1.18.2 does not allow configuration. Sorry!_

You can configure the behavior of the mod from the `inventorymanagement.toml` file within your config folder. If you have ModMenu installed, you can also access the configuration through the UI in ModMenu's mod list!

`modEnabled`: `true|false` - Simple toggle for the mod! Set to `false` to disable.

`showSort`: `true|false` - Whether or not to show sort buttons in the UI.

`showTransfer`: `true|false` - Whether or not to show transfer buttons in the UI.

`showStack`: `true|false` - Whether or not to show autostack buttons in the UI.

`guiTheme`: `"light"|"dark"|"auto"` - Whether the buttons should use light theme (vanilla), dark theme (VanillaTweaks dark UI), or automatically choose based on whether you have VanillaTweaks dark UI enabled.

`defaultPosition`: `"(<Integer>,<Integer>)"` - Customize a default for button position.

`screenPositions`: `{"ID": "(<Integer>,<Integer>)"}` - Customize button position on a per-screen basis. While this can be modified manually in the config file, the recommended way to modify this is through the configuration UI. See below!

### Modifying the button positions

In order to maintain compatibility with other mods, you can now adjust the positions of your inventory management buttons! To adjust the position for all buttons, check out the option in the config for `defaultPosition` or if you have ModMenu installed, in the config UI. If you have a particular screen that needs adjusted (maybe another mod added more inventory space, for example), you can hold `Ctrl/Cmd` and click on one of the UI buttons to open up the per-screen position editor! This editor will adjust the button positions for the current screen only, so you can tweak the positions all you want to get it aligned with your shiny backpack mod's UI! ;)

---

## Sort

![](https://i.imgur.com/Vcy2WL1.png)

A sort button will now appear in all containers (that are large enough) and your inventory. Clicking this button will sort the inventory automatically! For the most part this sorting is alphabetical, with a few manually-entered exceptions. Sorting ignores your hotbar, so you don't have to worry about it messing up your main items!

### Before

![](https://i.imgur.com/jt2uAGJ.png)

### After

![](https://i.imgur.com/0nwOaRO.png)

## Transfer all

![](https://i.imgur.com/hM52cuQ.png)

The place/take all buttons will transfer one entire inventory into the other! It starts at the top-left slot and works its way through the items until the other inventory is full. Similar to sorting, this mechanism will ignore your hotbar (and equipped items).

## Automatically stack

![](https://i.imgur.com/mpt6Ycz.png)

Similar to the transfer all buttons, there will also be buttons for stacking into/from a container. When clicked, this button will take all the items in the source inventory and stack them into any non-full stacks in the other, transferring only items required to fill up the stacks!

### Before

![](https://i.imgur.com/xG5e1ZW.png)

### After

![](https://i.imgur.com/yXyvZO6.png)
