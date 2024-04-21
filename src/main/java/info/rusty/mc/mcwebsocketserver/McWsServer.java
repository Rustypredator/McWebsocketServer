package info.rusty.mc.mcwebsocketserver;

import com.google.common.base.CaseFormat;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import info.rusty.mc.mcwebsocketserver.modules.PlayerPositions;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class McWsServer extends WebSocketServer {
	public static final Logger LOGGER = LoggerFactory.getLogger("McWebsocketServer");
	private InetSocketAddress address;
	private Map<String, List<WebSocket>> subscribers;
	private McWsServerBroadcastScheduler scheduler;

	public void shutdown() throws InterruptedException {
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
				if (McWebsocketServerConfig.INSTANCE.debug.value()) {
					String Ip = conn.getRemoteSocketAddress().getAddress().getHostAddress();
					LOGGER.info("Removing subscriber "+Ip+" from module: " + entry.getKey());
				}
				currentSubscribers.remove(conn);
				subscribers.put(entry.getKey(), currentSubscribers);
			}
		}
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		//discard empty messages:
		if (message.isEmpty()) {
			if (McWebsocketServerConfig.INSTANCE.debug.value()) {
				String Ip = conn.getRemoteSocketAddress().getAddress().getHostAddress();
				LOGGER.info("Received empty Message from "+Ip);
			}
			return;
		}
		// debug info
		if (McWebsocketServerConfig.INSTANCE.debug.value()) {
			LOGGER.info("Received Message: " + message);
		}
		// Handle message.
		if (message.equals("ping")) {
			conn.send("pong");
			return;
		}
		// If the message is in json format, parse it and handle it.
		Gson gson = new Gson();
		JsonObject messageJson;
		try {
			messageJson = gson.fromJson(message, JsonObject.class); // Parse the message into a JSON object.
		} catch (Exception e) {
			LOGGER.info("Error while parsing JSON message.", e);
			LOGGER.info("Message: " + message);
			return;
		}
		// Check if the message contains the module name.
		if (!messageJson.has("module")) {
			LOGGER.info("Message does not contain module name.");
			LOGGER.info("Message: " + message);
			conn.send(new JsonMessageBuilder("success=false,error=Incomplete Request.").toJson());
			return;
		}
		// Check if the message contains the mode.
		if (!messageJson.has("mode")) {
			LOGGER.info("Message does not contain mode.");
			LOGGER.info("Message: " + message);
			conn.send(new JsonMessageBuilder("success=false,error=Incomplete Request.").toJson());
			return;
		}
		String module = messageJson.get("module").getAsString(); // Get the module name.
		String mode = messageJson.get("mode").getAsString(); // Get the mode.
		//check if module is enabled:
		boolean enabled = Boolean.parseBoolean(McWebsocketServerConfig.INSTANCE.modules.value().get(module));
		if (enabled) {
			moduleSwitcher(module, mode, conn, messageJson);
		} else {
			if (McWebsocketServerConfig.INSTANCE.debug.value()) {
				String Ip = conn.getRemoteSocketAddress().getAddress().getHostAddress();
				LOGGER.info("Client "+Ip+" tried querying disabled module: " + module);
			}
			conn.send(new JsonMessageBuilder("success=false,error=Module is not enabled.").toJson());
		}
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		LOGGER.trace("an error occurred on connection", ex);
	}

	@Override
	public void onStart() {
		LOGGER.info("Server started");
		// Make sure the subscriber list is initialized:
		if (subscribers == null) {
			subscribers = new HashMap<>();
		}
	}

	public void moduleSwitcher(String module, String mode, WebSocket conn, JsonObject message) {
		List<String> whitelist;
		switch (module) {
			case "player_positions":
				//check if IP is allowed:
				whitelist = McWebsocketServerConfig.INSTANCE.playerPositionsAllowedIps.value();
				if (!this.whitelistCheck(conn, module, whitelist)) {return;}
				// Handle player_positions module.
				if (mode.equals("once")) {
					if (McWebsocketServerConfig.INSTANCE.debug.value()) {
						String Ip = conn.getRemoteSocketAddress().getAddress().getHostAddress();
						LOGGER.info("Client "+Ip+" requested one-time info: " + module);
					}
					PlayerPositions ppm = new PlayerPositions();
					ppm.handleMessage(conn, message);
				} else if (mode.equals("subscribe")) {
					this.subscribe(conn, module);
				}
				break;
			case "player_chat":
				//check if IP is allowed:
				whitelist = McWebsocketServerConfig.INSTANCE.playerChatAllowedIps.value();
				if (!this.whitelistCheck(conn, module, whitelist)) {return;}
				// Handle player_chat module.
				break;
			case "player_join_leave":
				//check if IP is allowed:
				whitelist = McWebsocketServerConfig.INSTANCE.playerJoinLeaveAllowedIps.value();
				if (!this.whitelistCheck(conn, module, whitelist)) {return;}
				// Handle player_join_leave module.
				break;
			case "player_death":
				//check if IP is allowed:
				whitelist = McWebsocketServerConfig.INSTANCE.playerDeathAllowedIps.value();
				if (!this.whitelistCheck(conn, module, whitelist)) {return;}
				// Handle player_death module.
				break;
			case "player_advancement":
				//check if IP is allowed:
				whitelist = McWebsocketServerConfig.INSTANCE.playerAdvancementAllowedIps.value();
				if (!this.whitelistCheck(conn, module, whitelist)) {return;}
				// Handle player_advancement module.
				break;
			case "player_inventory":
				//check if IP is allowed:
				whitelist = McWebsocketServerConfig.INSTANCE.playerInventoryAllowedIps.value();
				if (!this.whitelistCheck(conn, module, whitelist)) {return;}
				// Handle player_inventory module.
				break;
			default:
				this.sendNotWhitelisted(conn);
				break;
		}
	}

	public void subscriberBroadcast(String module) {
		//skip if no one connected:
		if (this.getConnections().isEmpty()) {
			if (McWebsocketServerConfig.INSTANCE.debug.value()) {
				LOGGER.info("No one connected. Skipping subscriber broadcast.");
			}
			return;
		}
		// If Module == null then broadcast all enabled modules.
		try {
			//check if module has subscribers:
			if (!subscribers.containsKey(module) || subscribers.get(module).isEmpty()) {
				if (McWebsocketServerConfig.INSTANCE.debug.value()) {
					LOGGER.info("No subscribers for module: " + module);
				}
				return;
			}
			List<WebSocket> currentSubscribers = subscribers.get(module);
			for (WebSocket subscriber : currentSubscribers) {
				moduleSwitcher(module, "once", subscriber, null);
			}
		} catch (Exception e) {
			LOGGER.error("Error while broadcasting to subscribers", e);
		}
	}

	private boolean whitelistCheck(WebSocket conn, String module, List<String> whitelist) {
		String ipAddress = conn.getRemoteSocketAddress().getAddress().getHostAddress();
		if (whitelist.contains(ipAddress) || whitelist.contains("*")) {
			return true;
		}
		this.sendNotWhitelisted(conn);
		if (McWebsocketServerConfig.INSTANCE.debug.value()) {
			String Ip = conn.getRemoteSocketAddress().getAddress().getHostAddress();
			LOGGER.info("Client "+Ip+" failed Whitelist Check for module: " + module);
		}
		return false;
	}

	private void subscribe(WebSocket conn, String module) {
		// Initialize subscribers list if it is null.
		if (subscribers == null) {
			subscribers = new HashMap<>();
		}
		// Create empty List if the module is not present in the subscribers list.
        subscribers.computeIfAbsent(module, k -> new java.util.ArrayList<>());
		// Get Current Subscribers
		List<WebSocket> currentSubscribers = subscribers.get(module);
		// Add the new subscriber to the list.
		currentSubscribers.add(conn);
		// Update the subscribers list.
		subscribers.put(module, currentSubscribers);
		// Check if the subscriber was added successfully.
		String Ip = conn.getRemoteSocketAddress().getAddress().getHostAddress();
		if (subscribers.get(module).contains(conn)) {
			if (McWebsocketServerConfig.INSTANCE.debug.value()) {
				LOGGER.info("Adding Subscriber "+Ip+" for module: " + module);
			}
			conn.send(new JsonMessageBuilder("success=true,message=Subscription Successful.").toJson());
		} else {
			if (McWebsocketServerConfig.INSTANCE.debug.value()) {
				LOGGER.info("Subscription for Client "+Ip+" to module: " + module +  " failed.");
			}
			conn.send(new JsonMessageBuilder("success=false,error=Subscription Failed.").toJson());
		}
	}

	private void sendNotWhitelisted(WebSocket conn) {
		conn.send(new JsonMessageBuilder("success=false,error=Your IP is not whitelisted for this module.").toJson());
	}
}
