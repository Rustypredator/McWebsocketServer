package info.rusty.mc.mcwebsocketserver;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import info.rusty.mc.mcwebsocketserver.modules.PlayerPositions;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

class McWsServer extends WebSocketServer {
	public static final Logger LOGGER = LoggerFactory.getLogger("McWebsocketServer");
	private InetSocketAddress address;
	private Map<String, List<WebSocket>> subscribers;
	private McWsServerBroadcastScheduler scheduler;

	public void shutdown() throws InterruptedException, SchedulerException {
		scheduler.shutdown(); //stop the scheduler
		this.stop(); //stop the websocket server
	}

	public McWsServer(InetSocketAddress address) {
		super(address);
		this.address = address;
		//Start a new Scheduler:
		int delay = McWebsocketServerConfig.INSTANCE.broadcastDelay.value();
		try {
			scheduler = new McWsServerBroadcastScheduler(this, delay);
		} catch (Exception e) {
			LOGGER.error("Error while starting scheduler", e);
		}
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		if (McWebsocketServerConfig.INSTANCE.debug.value()) {
			LOGGER.info("mew Client connected!");
		}
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		if (McWebsocketServerConfig.INSTANCE.debug.value()) {
			LOGGER.info("Client left!");
		}
		//remove client from subscribers list if present:
		for (Map.Entry<String, List<WebSocket>> entry : subscribers.entrySet()) {
			List<WebSocket> currentSubscribers = entry.getValue();
			if (currentSubscribers.contains(conn)) {
				currentSubscribers.remove(conn);
				subscribers.put(entry.getKey(), currentSubscribers);
			}
		}
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		//discard empty messages:
		if (message.isEmpty()) {
			return;
		}
		// debug info
		if (McWebsocketServerConfig.INSTANCE.debug.value()) {
			LOGGER.info("Received Message: " + message);
		}
		// Handle message.
		// If the message is not in json format, discard it.
        try {
            if (!JSONUtils.isJSONValid(message)) {
                return;
            }
        } catch (Exception e) {
            LOGGER.error("Error while parsing JSON", e);
            return;
        }
		// If the message is in json format, parse it and handle it.
		Gson gson = new Gson();
		JsonObject messageJson = gson.fromJson(message, JsonObject.class); // Parse the message into a JSON object.
		String module = messageJson.get("module").getAsString(); // Get the module name.
		String mode = messageJson.get("mode").getAsString(); // Get the mode.
		//check if module is enabled:
		boolean enabled = Boolean.parseBoolean(McWebsocketServerConfig.INSTANCE.modules.value().get(module));
		if (enabled) {
			moduleSwitcher(module, mode, conn, messageJson);
		} else {
			conn.send("Module " + module + " is not enabled.");
		}
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		if (conn != null) {
			// some errors like port binding failed may not be assignable to a specific websocket
		}
		LOGGER.trace("an error occurred on connection", ex);
	}

	@Override
	public void onStart() {
		LOGGER.info("Server started");
	}

	public void moduleSwitcher(String module, String mode, WebSocket conn, JsonObject message) {
		switch (module) {
			case "player_positions":
				//check if IP is allowed:
				if (!McWebsocketServerConfig.INSTANCE.playerPositionsAllowedIps.value().contains(conn.getRemoteSocketAddress().getAddress().getHostAddress())) {
					conn.send("Your IP is not whitelisted for this module.");
					return;
				}
				// Handle player_positions module.
				if (mode.equals("once")) {
					PlayerPositions ppm = new PlayerPositions();
					ppm.handleMessage(conn, message);
				} else if (mode.equals("subscribe")) {
					//Update subscribers list:
					List<WebSocket> currentSubscribers = subscribers.get("player_positions");
					currentSubscribers.add(conn);
					subscribers.put("player_positions", currentSubscribers);
				}
				break;
			case "player_chat":
				//check if IP is allowed:
				if (!McWebsocketServerConfig.INSTANCE.playerChatAllowedIps.value().contains(conn.getRemoteSocketAddress().getAddress().getHostAddress())) {
					conn.send("Your IP is not whitelisted for this module.");
					return;
				}
				// Handle player_chat module.
				break;
			case "player_join_leave":
				//check if IP is allowed:
				if (!McWebsocketServerConfig.INSTANCE.playerJoinLeaveAllowedIps.value().contains(conn.getRemoteSocketAddress().getAddress().getHostAddress())) {
					conn.send("Your IP is not whitelisted for this module.");
					return;
				}
				// Handle player_join_leave module.
				break;
			case "player_death":
				//check if IP is allowed:
				if (!McWebsocketServerConfig.INSTANCE.playerDeathAllowedIps.value().contains(conn.getRemoteSocketAddress().getAddress().getHostAddress())) {
					conn.send("Your IP is not whitelisted for this module.");
					return;
				}
				// Handle player_death module.
				break;
			case "player_advancement":
				//check if IP is allowed:
				if (!McWebsocketServerConfig.INSTANCE.playerAdvancementAllowedIps.value().contains(conn.getRemoteSocketAddress().getAddress().getHostAddress())) {
					conn.send("Your IP is not whitelisted for this module.");
					return;
				}
				// Handle player_advancement module.
				break;
			case "player_inventory":
				//check if IP is allowed:
				if (!McWebsocketServerConfig.INSTANCE.playerInventoryAllowedIps.value().contains(conn.getRemoteSocketAddress().getAddress().getHostAddress())) {
					conn.send("Your IP is not whitelisted for this module.");
					return;
				}
				// Handle player_inventory module.
				break;
			default:
				// Handle unknown module.
				break;
		}
	}

	public void publicBroadcast() {
		// Broadcast message to all clients.
		this.broadcast("Public Broadcast");
		//get all modules that are configured to be broadcast:
		Map<String, String> broadcastModules = McWebsocketServerConfig.INSTANCE.broadcastModules.value();
	}

	public void subscriberBroadcast() {
		// Broadcast message to individual subscribers.
		this.broadcast("Subscriber Broadcast");
	}
}
