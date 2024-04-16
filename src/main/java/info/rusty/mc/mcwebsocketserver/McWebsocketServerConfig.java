package info.rusty.mc.mcwebsocketserver;

import org.quiltmc.config.api.ReflectiveConfig;
import org.quiltmc.config.api.annotations.Comment;
import org.quiltmc.config.api.annotations.SerializedName;
import org.quiltmc.config.api.values.TrackedValue;
import org.quiltmc.config.api.values.ValueList;
import org.quiltmc.config.api.values.ValueMap;
import org.quiltmc.loader.api.config.v2.QuiltConfig;

/**
 * Main Config file for McWebsocketServer
 */
public class McWebsocketServerConfig extends ReflectiveConfig {
	public static final McWebsocketServerConfig INSTANCE = QuiltConfig.create("McWebsocketServer", "main", McWebsocketServerConfig.class);
	@Comment("The address where the server should listen on. ONLY NUMBERS AND DOTS ALLOWED, DO NOT USE localhost or similar!")
	@SerializedName("server_address")
	public final TrackedValue<String> serverAddress = this.value("127.0.0.1");
	@Comment("The port where the server should listen on.")
	@SerializedName("server_port")
	public final TrackedValue<Integer> serverPort = this.value(25566);
	@Comment("The debug mode of the server.")
	@SerializedName("debug")
	public final TrackedValue<Boolean> debug = this.value(false);
	@Comment("The delay in seconds between broadcasts. 0 means no delay. Don't set it to low, as it can cause lag!")
	@SerializedName("broadcast_delay")
	public final TrackedValue<Integer> broadcastDelay = this.value(10);
	@Comment("The Module Config. True/false to enable or disable the module. Default is false.")
	@SerializedName("modules")
	public final TrackedValue<ValueMap<String>> modules = this.map("")
		.put("player_positions", "false")
		.put("player_chat", "false")
		.put("player_join_leave", "false")
		.put("player_death", "false")
		.put("player_advancement", "false")
		.put("player_inventory", "false")
		.build();

	@Comment("Which modules should Broadcast? Default is none. (Please remember that broadcasting inventories can generate a lot of data that might lag your server!) Enabling Broadcasting here, disables the IP check in the next section!")
	@SerializedName("broadcast_modules")
	public final TrackedValue<ValueMap<String>> broadcastModules = this.map("")
		.put("player_positions", "false")
		.put("player_chat", "false")
		.put("player_join_leave", "false")
		.put("player_death", "false")
		.put("player_advancement", "false")
		.put("player_inventory", "false")
		.build();

	@Comment("Allowed IP Config for Modules. this can have multiple allowed IPs or * for all IPs")
	@Comment("Using * allows any client to subscribe to the module. This is not recommended, as it can cause a lot of traffic, and may leak sensitive information!")
	@Comment("For now, partial wildcards like 192.168.178.* are not supported!")
	@SerializedName("player_positions_allowed_ips")
	public final TrackedValue<ValueList<String>> playerPositionsAllowedIps = this.list("","127.0.0.1", "192.168.178.1");
	@SerializedName("player_chat_allowed_ips")
	public final TrackedValue<ValueList<String>> playerChatAllowedIps = this.list("","127.0.0.1", "192.168.178.1");
	@SerializedName("player_join_leave_allowed_ips")
	public final TrackedValue<ValueList<String>> playerJoinLeaveAllowedIps = this.list("","127.0.0.1", "192.168.178.1");
	@SerializedName("player_death_allowed_ips")
	public final TrackedValue<ValueList<String>> playerDeathAllowedIps = this.list("","127.0.0.1", "192.168.178.1");
	@SerializedName("player_advancement_allowed_ips")
	public final TrackedValue<ValueList<String>> playerAdvancementAllowedIps = this.list("","127.0.0.1", "192.168.178.1");
	@SerializedName("player_inventory_allowed_ips")
	public final TrackedValue<ValueList<String>> playerInventoryAllowedIps = this.list("","127.0.0.1", "192.168.178.1");
}
