//Parts of this file are copied from Brandon Brooks's Node Implementation: https://github.com/ArchmageInc/minecraft-player-locations

//add servers here and associate them with worlds:
let servers = [
    {
        type: "bukkit",
        host: "srv1.htav.de",
        port: 1307,
        worlds: ["lobby"],
    },
]

window.PlayerLocations = {
    debug: true,
    server: {},
    connection: {},
    playerMarkers: {}, //list of markers for map
    
    createPlayerMarkers: (list) => {
        //check if player still exists in list:
        for (let playerName in window.PlayerLocations.playerMarkers) {
            if( !list.hasOwnProperty(playerName) ) {
                //players name was not found in the list, remove it from markers
                window.PlayerLocations.playerMarkers[playerName].remove();
                delete window.PlayerLocations.playerMarkers[playerName];
            }
        }
        //loop current players
        for (let i in list) {
            let playerData = list[i];
            let location = playerData.location;
            let playerName = playerData.name;
            let uuid = playerData.uuid;
            let latlng = overviewer.util.fromWorldToLatLng(location.x, location.y, location.z, window.PlayerLocations.getCurrentTileSet());

            if (window.PlayerLocations.playerMarkers[i]) {
                //set coordinates for player if he is already in the list
                window.PlayerLocations.playerMarkers[i].setLatLng(latlng);
            } else {
                //if the player was not previously in the list, create a new icon
                let icon =L.icon({
                    iconUrl: "https://crafatar.com/avatars/" + uuid + "?size=16&default=MHF_Steve",
                    iconSize: [16, 16],
                    iconAnchor: [15, 17]
                });
                //create new marker for the player
                let marker = L.marker(latlng, {
                    icon: icon,
                    title: playerName
                });
                //add new marker to current map
                marker.addTo(overviewer.map);
                //add marker to the list
                window.PlayerLocations.playerMarkers[i] = marker;
            }
        }
    },
    
    getCurrentTileSet: () => {
        let name = overviewer.current_world;
        for (let index in overviewerConfig.tilesets) {
            let tileset = overviewerConfig.tilesets[index];
            if (tileset.world === name) {
                return tileset;
            }
        }
    },
    
    initialize: () => {
        //determine wich server to connect to based upon wich tileset we are using:
        let name = overviewer.current_world; //check if we find this world in any servers
        let host = "";
        let port = 8888;
        for(let i in servers) {
            let server = servers[i];
            console.info("server " + i + " has: " + server.worlds);
            if(server.worlds.includes(name)) {
                console.info("Server " + i + " Does have the correct world: " + name + "! Connecting...." );
                //set used server
                window.PlayerLocations.server = server;
                host = server.host;
                port = server.port;
                window.PlayerLocations.connect(host, port);
            } else {
                console.info("Server " + i + " Does not have the correct world: " + name);
            }
        }
    },
    
    connect: (host, port) => {
        let socketUrl = 'wss://' + host + ':' + port;
        let connection = new WebSocket(socketUrl);
        window.PlayerLocations.connection = connection;
        connection.onopen = () => {
            console.info('WebSocket Connection opened');
        };
        connection.onerror = (error) => {
            console.error(`WebSocket error ${error}`);
            setTimeout(window.PlayerLocations.connect, 3000);
        };
        connection.onmessage = (msg) => {
            //sample: [{"name":"Rustypredator","uuid":"9bbbee7f-e0ab-4694-a7de-1e06a25715ae","location":{"x":1234,"y":56,"z":789}}]
            try{
                let data = JSON.parse(msg.data);
                if(window.PlayerLocations.debug) {
                    console.info('WebSocket received data:', data);
                }
                window.PlayerLocations.createPlayerMarkers(data);
            } catch(error) {
                console.error('Error parsing WebSocket message', error);
            }
        };
        connection.onclose = () => {
            console.info('WebSocket Connection closed');
            setTimeout(window.PlayerLocations.connect(), 3000);
        };
    }
};

overviewer.util.ready(window.PlayerLocations.initialize);
//attatch event listener to the worldcontrol select to detect when the world is changed, then reload websocket
var worldctrl = document.querySelectorAll(".worldcontrol select"), attatch = function() {
    [].map.call(worldctrl, function(elem) {
        elem.addEventListener("change", window.PlayerLocations.initialize, false);
    });
};
