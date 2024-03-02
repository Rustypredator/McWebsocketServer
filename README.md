# WebSocketServer
  
Delivers live data from the server it is running on via a websocket server.  
This data could be used by a variety of applications. For example: display live player-Markers on a Minecraft Map.  
The Plugin/Mod can be configured in modules and can Broadcast, or respond to requests on demand.  
For Example, you can configure the Server to broadcast all player positions every x second, and request the inventory of a specific player only when needed.  
  
Example for using the player Positions on webmaps: [Readme](MapPositions/README.md)

## Installation on the minecraft server

Download the latest release from the [Spigot Resource](https://www.spigotmc.org/resources/playerposwebsockets.87136/), drop it in your plugins folder.
you can also get the latest .jar as a build artifact:
1. Open the CI menu (CI/CD on the left)
2. Find the last "passed" result and click the download icon at the right side.

## Configure

By default, all modules are disabled.  
Go into the configuration file, which should have been created for you, and enable the modules you want to use.  
Set the mode of the module:
- Broadcast: Sends the data on a specific interval, regardless of incoming messages.
- OnDemand: Only responds to the correct incoming messages with the data, otherwise this data is not sent.  

## 3rd Party Software
[Websocket Library](https://github.com/TooTallNate/Java-WebSocket)
