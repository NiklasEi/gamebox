# GameBox
[![Discord](https://img.shields.io/discord/205041952431931392.svg?label=&logo=discord&logoColor=ffffff&color=7389D8&labelColor=6A7EC2)](https://discord.gg/WgCrwXF)
[![Version](https://img.shields.io/spiget/version/37273.svg?label=version)](https://www.spigotmc.org/resources/37273/)
[![Downloads](https://img.shields.io/spiget/downloads/37273.svg)](https://www.spigotmc.org/resources/37273/)
[![Rating](https://img.shields.io/spiget/rating/37273.svg)](https://www.spigotmc.org/resources/37273/)


GameBox is a minecraft plugin written with the Bukkit API and published on [Spigot].

Many inventory games can be added to GameBox and can then be accessed by players through a single GUI. GameBox is configurable and all texts can be changed in the language files.

![GameBox main menu](gamebox.png "GameBox main menu with ten installed games")

## Features

* Growing number of single and multiplayer games
  * [List of all GameBox games][GameBox-games]
  * Write your own games! ([Template game][example-project])
* Customisable GUIs, texts, titles and game modes
* Statistics and top lists
* Support for MySQL and file storage
* Token system with configurable [shop](#shop)

GameBox uses language files for messages and inventory titles. You can add your own file or use one of the default files (German, English, Spanish and Chinese). [More info on the Wiki](https://github.com/NiklasEi/gamebox/wiki/Language).

All games have their own configuration and language files.

### Token

Token are a currency provided by GameBox. They can be awarded for winning games and spend to by items or other things in the [token shop](#shop). 

Tokens can be read, awarded, and taken through the [API](#api) and through admin commands.

### Shop

[Wiki page](https://github.com/NiklasEi/gamebox/wiki/Token-Shop)

The first view of the token shop is a Menu that lists all shop categories. Each category has pages filled with configurable shop items and is automatically paginated.

Per default players can access the shop with a button on the main GUI. This requires the permission **`gamebox.shop`**.

The shop can
* sell items for token and/or money
* run commands in exchange for token and/or money
* sell things based on the players permissions
  * an item can require the player to have a specific permission or to not have it
* have custom categories
* contain as many items in every category as you want

The shop can be configured in the file `tokenShop.yml` which is generated in the GameBox directory.

### API

GameBox provides an API to be used by other plugins.

Currently implemented features:
* Give/take/set token for online and offline players.
* Get the token count for an online or offline player.

[API source](src/main/java/me/nikl/gamebox/GameBoxAPI.java)

## Installation

1. Download the newest version from [Spigot].
2. Drop it in your servers plugin folder.
3. Give GameBox admin permissions to your administrators.
4. Restart your server.
5. Configure GameBox:
   1. Change the used language file in config.yml (if you need something else then english).
   2. You should enable the hub mode ;)
   3. You can customise your tokenShop.yml and open the shop for your players.
6. Players with GameBox administrator permissions (`gamebox.admin.*` or `gamebox.admin.modules`) can download new games (modules) from the GUI.
6. Optional: configure the games
   1. The games' configuration files are in `GameBox/games/<gameID>`
   2. The games' language files are in `GameBox/language/<gameID>`

### Custom builds

Just fork and clone this repository and run `mvn package`. The jar will be in the target folder in the root directory and is ready for distribution.

GameBox and my nmsutilities are hosted on [my Artifactory server][artifactory]. You can check there for up-to-date versions.

## Commands and permissions

Please refer to the plugins page for a detailed list of commands and permissions.

[Commands list][GameBox-cmds]

[Permissions list][GameBox-perms]

The permissions allow for different players having access to different games. By default, all players can play all games! To change that one has to take the permission `gamebox.play.*` from all players. Then add the game specific play permissions `gamebox.play.<gameID>`. The unique ids of all games can be found [on the project page][gamebox-ids].

[Spigot]: https://www.spigotmc.org/resources/37273/
[gamebox-ids]: https://www.nikl.me/projects/minecraft/gamebox/#ids
[GameBox-games]: https://www.nikl.me/projects/minecraft/gamebox/#games
[GameBox-cmds]: https://www.nikl.me/projects/minecraft/gamebox/#commands
[GameBox-perms]: https://www.nikl.me/projects/minecraft/gamebox/#permissions
[example-project]: https://github.com/NiklasEi/template-module-for-gamebox
[artifactory]: https://repo.repsy.io/mvn/nikl/minecraft
