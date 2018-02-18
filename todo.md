# GameBox version 2

* rewrite Statistics
  * better access
  * possibility to get any sublist of the top 100
  * fast calc of rank of given player
  * pull info from several/all games about one player
* Settings menu
  * sort the games in the main GUI
* new options on events. Also fire events, so that other plugins can listen for them. 
  * on invite:
    * possible action bar message
    * possible title/subtitle message
    * keep clickable message in chat to get to the invite
  * on game win:
    * fireworks?
    * sound effects
* commands
  * open game GUIS
  * start games
  * open shop
* option to white/blacklist (all)players in regard to invitations
* option for "invitation radius"
* rewrite permission system


Small stuff:
* check the bStats original game names (v1) against the default names in the lang files (used in v2). If they don't match there will be mess in the statistics...
* replace deprecated PlayerPickupItemEvent with EntityPickupItemEvent (uwaga! only in 1.12)

