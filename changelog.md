# Changelog

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