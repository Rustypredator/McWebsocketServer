package info.rusty.mc.mcwebsocketserver;

import org.quiltmc.config.api.ReflectiveConfig;
import org.quiltmc.config.api.annotations.Comment;
import org.quiltmc.config.api.annotations.SerializedName;
import org.quiltmc.config.api.values.TrackedValue;
import org.quiltmc.loader.api.config.v2.QuiltConfig;

/**
 * Main Config file for McWebsocketServer
 */
public class McWebsocketServerConfig extends ReflectiveConfig {
	public static final McWebsocketServerConfig INSTANCE = QuiltConfig.create("McWebsocketServer", "main", McWebsocketServerConfig.class);
	@Comment("The address where the server should listen on. Default is 127.0.0.1 ONLY NUMBERS AND DOTS ALLOWED, DO NOT USE localhost or similar!")
	@SerializedName("server_address")
	public final TrackedValue<String> serverAddress = this.value("127.0.0.1");
	@Comment("The port where the server should listen on. Default is 25566")
	@SerializedName("server_port")
	public final TrackedValue<Integer> serverPort = this.value(25566);
	@Comment("The debug mode of the server. Default is false")
	@SerializedName("debug")
	public final TrackedValue<Boolean> debug = this.value(false);
}
