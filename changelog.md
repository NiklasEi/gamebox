# Changelog

## 3.4.0
- support MC 1.17

## 3.3.1
- update Hikari and DataSource classes 

## 3.3.0
- Removed hardcoded parts in additional lore for shop items
  - the default items were adapted to include the previously hardcoded lines

## 3.2.0
- extend GameRuleMultiRewards to support CookieClicker rewards

## 3.1.2
- support MC 1.16.4

## 3.1.1
- support MC 1.16

## 3.1.0
- Add `/gba removesaves` commands
- Improve `/gba resetstats` commands
- fix spanish language file
- add new messages to spanish language file ([#84](https://github.com/NiklasEi/gamebox/pull/84))
- Print info messages when custom language files fail to load
- Improve error handling when loading modules from jar files

## 3.0.1
- Circumvent bug in Bukkit 1.14 (see: [TabooLib/TabooLib#41](https://github.com/TabooLib/TabooLib/issues/41))

## v3.0.0
- GameBox 3 supports MC 1.14+
- games can now be installed from inside GameBox. 
  - They are not separate plugins anymore, but can be automatically downloaded and installed.
  - Use commands or the module admin GUI to install/update/remove modules
- players only see games in the menu that they have the `gamebox.play` permission for
- games now always have the same order in the menu
- renamed language files to standard locales (new default file is en_GB)
- Loading database async ([#67](https://github.com/NiklasEi/gamebox/issues/67))

#

### v 2.2.6
- cookieclicker -> 2.2.3
  - Fix NPE on game startup
  - Fix issues with game saves on shutdown

### v 2.2.5
- cookieclicker -> v 2.2.2
- log loading of flatfile db ([#67](https://github.com/NiklasEi/gamebox/issues/67))
- only save players in the flatfile db if their settings were changed ([#65](https://github.com/NiklasEi/gamebox/issues/65))
- prevent exception on disable due to ACF ([#71](https://github.com/NiklasEi/gamebox/issues/71))
- remove old placeholder hook and adapt to new expansion system
- some cleanup of log messages

### v 2.2.4
- return null instead of throwing exception, when gb-player is unknown (expected behaviour)
   - fixes #57
- link to GitHub wiki for more info on token shop
- add command completion to all commands with arguments (#48)

### v 2.2.3
- stop players from accepting invitations in blocked worlds

### v 2.2.2
- stop picking up items when the event was canceled by another plugin
- fix removing of glow for mc version 1.13+
- add support for mc 1.13.1

### v 2.2.1
- cookieclicker -> v 2.2.1
  - command to manipulate number of cookies
- implement option for games to add commands to the gamebox commands
- cancel game invitations when the other player is in a disabled world
- make skull display names in top lists configurable
- fix ItemStackUtility throwing error on invalid materials, when a data value is given
- hide leftover debug messages
- fix high number names loaded from langauge file

### v 2.2.0
- compatibility with minecraft 1.13
- improved default colors in tokenShop and messages
- add high number (short-)names to the language file
- fixed debugging of the games...

### v 2.1.5
- update CookieClicker to 2.1.3
- update MatchIt to 1.0.1

### v 2.1.4
- use green record as symbol for "sound on"
- supply new utility method for logic puzzles plugin

### v 2.1.3
- cache and clone (player-)skulls
- bump shade plugin to fix problems with higher versions of Java

### v 2.1.2
- update CookieClicker to 2.1.2

### v 2.1.1
- update CookieClicker to 2.1.1

### v 2.1.0
- Bungee mode
   - new setting
   - sync top lists (only MySQL)
   - allow unknown players in top lists
- bump cookieclicker to 2.1.0
   - sync of saves for Bungeecord with MySQL
