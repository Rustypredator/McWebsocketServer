package info.rusty.mc.mcwebsocketserver.modules;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import info.rusty.mc.mcwebsocketserver.McWebsocketServer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Vec3d;
import com.google.gson.Gson;
import org.java_websocket.WebSocket;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerPositions {
	public void handleMessage(WebSocket conn, JsonObject message) {
		MinecraftServer server = McWebsocketServer.getServer();
		JsonArray playerPositions = new JsonArray();
		if (message == null || !message.has("uuid")) {
			//send all positions to client
			playerPositions = getAllPositions(server);
		} else {
			//get player by uuid
			PlayerEntity player = server.getPlayerManager().getPlayer(message.get("uuid").getAsString());
			//player may not be online.
			if (player == null) {
				return;
			}
			//send position to client
			playerPositions.add(getPosition(player));
		}
		// Send data to client
		JsonObject data = new JsonObject();
		data.addProperty("module", "player_positions");
		data.add("data", playerPositions);
		Gson j = new GsonBuilder().disableHtmlEscaping().create();
		conn.send(j.toJson(data));
	}
	private JsonObject getPosition(PlayerEntity player) {
		Vec3d position = player.getPos();
		//create Map with player name and position
		JsonObject playerInfo = new JsonObject();
		playerInfo.addProperty("player", player.getName().toString());
		playerInfo.addProperty("uuid", player.getUuid().toString());
		playerInfo.addProperty("world", player.getWorld().getRegistryKey().getValue().toString());
		playerInfo.addProperty("dimension", player.getWorld().getDimensionKey().getValue().toString());
		//create location object
		JsonObject playerLocation = new JsonObject();
		playerLocation.addProperty("x", String.valueOf(position.x));
		playerLocation.addProperty("y", String.valueOf(position.y));
		playerLocation.addProperty("z", String.valueOf(position.z));
		//add location to playerInfo
		playerInfo.add("location", playerLocation);
		//convert to string:
        return playerInfo;
	}
	private JsonArray getAllPositions(MinecraftServer server) {
		JsonArray players = new JsonArray();
		//check player count:
		if (server.getPlayerManager().getPlayerList().isEmpty()) {
			//no players online
			return players;
		}
		//get all players and call getPosition for each player
		server.getPlayerManager().getPlayerList().forEach(player -> {
			JsonObject playerInfo = getPosition(player);
            players.add(playerInfo);
		});
		return players;
	}
}
