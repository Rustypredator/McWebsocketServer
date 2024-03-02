package info.rusty.mcwebsocketserver;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Server;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class McWebSocketServer extends JavaPlugin {
	private WebSocketServer ws;
	private Metrics metrics;
	private Boolean debug;
	private Server server;
	private ConsoleCommandSender console;
	private ConsoleLog log;
	private InetAddress ip;
	private int port;
	private InetSocketAddress address;
	private int pluginId = 21187;
	private String dataFolder;

	@Override
	public void onEnable() {
		// default config
		this.saveDefaultConfig();
		this.dataFolder = this.getDataFolder().toString();
		// init server:
		this.server = this.getServer();
		// init console:
		this.console = server.getConsoleSender();
		// debug
		this.debug = this.getConfig().getBoolean("debug");
		// Logger
		this.log = new ConsoleLog(this.console, this.debug);
		// init bstats
		if (this.getConfig().getBoolean("bstats")) {
			this.log.info("Enabling bStats", null);
			this.metrics = new Metrics(this, this.pluginId);
		}
		//init server:
		this.port = this.getConfig().getInt("port");
		this.ip = com.google.common.net.InetAddresses.forString(this.getConfig().getString("ip"));
		this.address = new InetSocketAddress(this.ip, this.port);
		//create local wsserver:
		this.ws = new PlayerPosServer(this.address, this.log);
		ws.start();
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				if(ws.getConnections().size() < 1) {
					//skip
				} else {
					//create list of json objects:
					JsonArray playerList = new JsonArray();
					for(Player p : getServer().getOnlinePlayers()) {
						Location loc = p.getLocation();
						JsonObject playerData = new JsonObject();
						playerData.addProperty("name", p.getName());
						playerData.addProperty("uuid", p.getUniqueId().toString());
						playerData.addProperty("world", p.getWorld().getName());
						playerData.addProperty("health", p.getHealth());
						JsonObject playerLocation = new JsonObject();
						playerLocation.addProperty("x", loc.getBlockX());
						playerLocation.addProperty("y", loc.getBlockY());
						playerLocation.addProperty("z", loc.getBlockZ());
						playerData.add("location", playerLocation);
						playerList.add(playerData);
					}
					ws.broadcast(playerList.toString());
				}
			}
		}, 20, 20);
	}
	@Override
	public void onDisable() {
		this.log.info("Shutting down Gracefully", null);
		try {
			this.ws.stop();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.log.success("Shutdown complete!", null);
	}
}

class ConsoleLog {
	private ConsoleCommandSender console;
	private boolean debug;

	public ConsoleLog(ConsoleCommandSender console, boolean debug) {
		this.console = console;
		this.debug = debug;
	}

	private String HandlePrefix(String prefix) {
		if (prefix == null) {
			return "[McWs]";
		} else {
			return "[McWs|" + prefix + "]";
		}
	}

	private void log(String message, String prefix) {
		prefix = HandlePrefix(prefix);
		this.console.sendMessage(ChatColor.WHITE + prefix + message);
	}

	public void debug(String message, String prefix) {
		if(this.debug) {
			message = ChatColor.GRAY + "[DEBUG]" + ChatColor.WHITE + " " + message;
			this.log(message, prefix);
		}
	}

	public void info(String message, String prefix) {
		message = ChatColor.AQUA + "[INFO]" + ChatColor.WHITE + " " + message;
		this.log(message, prefix);
	}

	public void error(String message, String prefix) {
		message = ChatColor.RED + "[ERROR]" + ChatColor.WHITE + " " + message;
		this.log(message, prefix);
	}

	public void success(String message, String prefix) {
		message = ChatColor.GREEN + "[SUCCESS]" + ChatColor.WHITE + " " + message;
		this.log(message, prefix);
	}
}

class PlayerPosServer extends WebSocketServer {
	private InetSocketAddress port;
	private ConsoleLog log;

	public PlayerPosServer(InetSocketAddress port, ConsoleLog log) {
		super(port);
		this.port = port;
		this.log = log;
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		this.log.debug("new Client connected!", "WS");
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		this.log.debug("Client left!", "WS");
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		this.log.debug("Received Message: " + message, "WS");
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		ex.printStackTrace();
		if (conn != null) {
			// some errors like port binding failed may not be assignable to a specific websocket
		}
		this.log.error("Encountered Error: " + ex, "WS");
	}

	@Override
	public void onStart() {
		this.log.success("Server started on " + this.port, "WS");
	}
}
