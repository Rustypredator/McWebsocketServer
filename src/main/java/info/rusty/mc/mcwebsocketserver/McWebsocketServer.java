package info.rusty.mc.mcwebsocketserver;

import net.minecraft.server.MinecraftServer;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents;
import org.quiltmc.qsl.networking.api.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class McWebsocketServer implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("McWebsocketServer");
	private static McWsServer wsserver;
	private static MinecraftServer mcServer;

	@Override
	public void onInitialize(ModContainer mod) {
		registerServerStarted();
		ServerLifecycleEvents.STOPPING.register(server -> {
            try {
                wsserver.shutdown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
	}

	public void registerServerStarted() {
		//Server started event
		ServerLifecycleEvents.READY.register(server -> {
			mcServer = server;
			// Load config variables
			InetAddress serverAddress = com.google.common.net.InetAddresses.forString(McWebsocketServerConfig.INSTANCE.serverAddress.value());
			int serverPort = McWebsocketServerConfig.INSTANCE.serverPort.value();
			//Init WS Server:
			wsserver = new McWsServer(new InetSocketAddress(serverAddress, serverPort));
			wsserver.start();
			LOGGER.info("Server started on " + serverAddress + ":" + serverPort);
		});
		//Player Join event
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			//send player join message to all subscribers
			wsserver.subscriberBroadcast("player_join_leave");
		});
		//Player Disconnect event
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			//send player leave message to all subscribers
			wsserver.subscriberBroadcast("player_join_leave");
		});
		//Player Chat event
		//Player Death event
		//Player Advancement event
	}

	public static MinecraftServer getServer() {
		return mcServer;
	}
}
