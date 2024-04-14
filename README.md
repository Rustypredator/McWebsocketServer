# WebSocketServer
  
Delivers live data from the server it is running on via a websocket server.  
This data could be used by a variety of applications. Examples:
- display live player-Markers on a Minecraft Map.  
  
The Plugin/Mod can be configured in modules and can Broadcast or respond to requests on demand.  
Examples:
- broadcast all player positions every x second
- Provide the inventory of a specific player when requested.
  
Example for using the player Positions on webmaps: [Readme](MapPositions/README.md)

## Installation on the minecraft server

Download the latest release from the [Modrinth Page](https://modrinth.com/plugin/websocketserver), drop it in your plugins/mods folder.

## Configure

By default, all modules are disabled.  
Go into the configuration file, which should have been created for you, and enable the modules you want to use.  
Set the mode of the module:
- Broadcast: Sends the data on a specific interval, regardless of incoming messages.
- OnDemand: Only responds to the correct incoming messages with the data, otherwise this data is not sent.  

## Versions

### Spigot/Bukkit/Paper
|MC Version(s)|Plugin Version|Link|
|---|---|---|
|1.16 - 1.20.4|0.0.3|[0.0.3](https://github.com/Rustypredator/McWebsocketServer/tree/e6caa6c8a7d2690a5771e4c7e0045d7b6e341165)|

## 3rd Party Software
[Websocket Library](https://github.com/TooTallNate/Java-WebSocket)
