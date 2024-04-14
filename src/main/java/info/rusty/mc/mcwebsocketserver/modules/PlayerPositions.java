package info.rusty.mc.mcwebsocketserver.modules;

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
        //check if message contains a uuid or not.
		if (message.has("uuid")) {
			//get player by uuid
			PlayerEntity player = server.getPlayerManager().getPlayer(message.get("uuid").getAsString());
			//player may not be online.
			if (player == null) {
				return;
			}
			//send position to client
			conn.send(getPosition(player));
		} else {
			//send all positions to client
			conn.send(getAllPositions(server));
		}
	}
	private String getPosition(PlayerEntity player) {
		Vec3d position = player.getPos();
		//create Map with player name and position
		Map<String, String> playerInfo = new HashMap<>();
		playerInfo.put("player", String.valueOf(player.getName()));
		playerInfo.put("uuid", String.valueOf(player.getUuid()));
		playerInfo.put("world", String.valueOf(player.getWorld().getRegistryKey().getValue()));
		playerInfo.put("dimension", String.valueOf(player.getWorld().getDimensionKey()));
		playerInfo.put("x", String.valueOf(position.x));
		playerInfo.put("y", String.valueOf(position.y));
		playerInfo.put("z", String.valueOf(position.z));
		//convert to json:
        return new Gson().toJson(playerInfo); //TODO: format is broken, but it's fine for now.
	}
	private String getAllPositions(MinecraftServer server) {
		List<String> players = new java.util.ArrayList<>();
		//check player count:
		if (server.getPlayerManager().getPlayerList().isEmpty()) {
			//no players online
			return "[]";
		}
		//get all players and call getPosition for each player
		server.getPlayerManager().getPlayerList().forEach(player -> {
			String playerInfo = getPosition(player);
            players.add(playerInfo);
		});
		return new Gson().toJson(players); //TODO: format is broken, but it's fine for now.
	}
}
