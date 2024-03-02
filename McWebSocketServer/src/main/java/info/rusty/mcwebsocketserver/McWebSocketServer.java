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
import java.net.InetSocketAddress;

public class McWebSocketServer extends JavaPlugin {
	private WebSocketServer ws;
	@Override
	public void onEnable() {
		int pluginId = 21187;
		Metrics metrics = new Metrics(this, pluginId);
		this.saveDefaultConfig();
		boolean debug = this.getConfig().getBoolean("debug");
		InetSocketAddress port = new InetSocketAddress(this.getConfig().getInt("Port"));
		Server server = this.getServer();
		final ConsoleCommandSender console = server.getConsoleSender();
		//print init info:
		printInitInfo(this.getConfig().getInt("Port"), this.getDataFolder().toString(), console);
		//create local wsserver:
		this.ws = new PlayerPosServer(port, console, debug);
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
		//
	}

	private void printInitInfo(int port, String dataFolder, ConsoleCommandSender console) {
		console.sendMessage(ChatColor.AQUA + "################################");
		console.sendMessage(ChatColor.AQUA + "#" + ChatColor.WHITE + " WebSocketServer Initialized!");
		console.sendMessage(ChatColor.AQUA + "#" + ChatColor.WHITE + " Creating Server on " + port);
		console.sendMessage(ChatColor.AQUA + "################################");
	}
}

class PlayerPosServer extends WebSocketServer {
	private InetSocketAddress port;
	private ConsoleCommandSender console;
	private boolean debug;

	public PlayerPosServer(InetSocketAddress port, ConsoleCommandSender console, boolean debug) {
		super(port);
		this.port = port;
		this.console = console;
		this.debug = debug;
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		if(this.debug) {
			this.console.sendMessage(ChatColor.AQUA + "[Playerpositions][WS]" + ChatColor.GRAY + "[DEBUG]" + ChatColor.WHITE + " new Client connected!");
		}
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		if(this.debug) {
			this.console.sendMessage(ChatColor.AQUA + "[Playerpositions][WS]" + ChatColor.GRAY + "[DEBUG]" + ChatColor.WHITE + " Client left!");
		}
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		//ignore, we dont care about sent messages right?
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		ex.printStackTrace();
		if (conn != null) {
			// some errors like port binding failed may not be assignable to a specific websocket
		}
		this.console.sendMessage(ChatColor.AQUA + "[Playerpositions][WS]" + ChatColor.RED + "[ERROR]" + ChatColor.WHITE + " Encountered Error: " + ex);
	}

	@Override
	public void onStart() {
		this.console.sendMessage(ChatColor.AQUA + "[Playerpositions][WS]" + ChatColor.GREEN + "[SUCCESS]" + ChatColor.WHITE + " Websocket started!");
	}
}
