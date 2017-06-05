# GameBox

Give access to several inventory games through one clean GUI.

## Features

* Customize the GUI, messages, titles and game modes.
* A growing number of single and multiplayer games. For a complete list of games please refer to the [plugins page on my website][GameBox-games].
* Gives access to a token system with configurable shop (see [shop](#shop))

GameBox uses language files for messages and inventory titles. You can add your own file or use one of the default files (german, english, spanish and mandarin) that are included in the plugin. The name of the used language file is specified in the configuration file of GameBox.

All games have their own language files. The system for the games is the same as for GameBox.

### Token

Token are a currency provided by GameBox. They can be awarded for won games and can be used to sell items or other things in the [GameBox shop](#shop). 

They can be accessed and manipulated through the [API](#api) and through admin commands.

### Shop

The GameBox shop consists of a Menu that lists all shop categories and of pages full of shop items for each category. Navigation is done by 'Forward' and 'Backward' buttons.

Per default access to the shop is given through a button on the main gui. The needed permission is **`gamebox.shop`**
and is given to all players by default.

* Sell items for token and/or money.
* Run commands for token and/or money.
  * There is a special option for commands that manipulate the players inventory.
* Add your own Shop categories with configurable name, lore and button.
* Items per category are basically unlimited. An automatic page system will create shop pages.

### API

GameBox provides an API.

Currently implemented features in the API:
* Give/take/set token for online and offline players.
* Get the amount of token a (offline-) player has.

[API source](src/me/nikl/gamebox/GameBoxAPI.java)

## Commands and permissions

Please refer to the plugins page for a detailed list of commands and permissions.

[Commands list][GameBox-cmds]

[Permissions list][GameBox-perms]

[GameBox-games]: www.nikl.me/projects/gamebox/#games
[GameBox-cmds]: www.nikl.me/projects/gamebox/#commands
[GameBox-perms]: www.nikl.me/projects/gamebox/#permissions