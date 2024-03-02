# PlayerPosWs.js

The "playerposws.js" file currently supports the following mapping Programs:  
- [Minecraft Overviewer](https://overviewer.org) ( Basic Functionality complete )
- [Mapcrafter](https://github.com/mapcrafter/mapcrafter) ( In progress )
  
# Demo Maps: 
Add your own via a PR ;)
| Minecraft Server-Ip | Map address | Maprenderer |
|---------------------|-------------|-------------|
|mc.htav.de|https://htav.de/mcmaps|Overviewer, Mapcrafter|
  
# Configure the WebsocketServer:

1. Set The PlayerPositions module to broadcast

# Installation for Overviewer
  
1. Add the playerposws.js file to your asset-folder.
2. Add this line to the index.html file (also in your asset folder): `<script type="text/javascript" src="path/to/playerposws.js"></script>` Below all the other `<script>` Tags.

Known Issues:
- Refresh needed to change backend server #1
- Displays all players on one server regardless of world #2