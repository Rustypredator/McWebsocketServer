package info.rusty.mc.mcwebsocketserver;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class McWebsocketServer implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod name as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("McWebsocketServer");
	private McWsSServer server;

	@Override
	public void onInitialize(ModContainer mod) {
		// Load config variables
		InetAddress serverAddress = com.google.common.net.InetAddresses.forString(McWebsocketServerConfig.INSTANCE.serverAddress.value());
		int serverPort = McWebsocketServerConfig.INSTANCE.serverPort.value();
		//Init WS Server:
		this.server = new McWsSServer(new InetSocketAddress(serverAddress, serverPort));
		server.start();
		LOGGER.info("Server started on " + serverAddress + ":" + serverPort);
	}
}

class McWsSServer extends WebSocketServer {
	public static final Logger LOGGER = LoggerFactory.getLogger("McWebsocketServer");
	private InetSocketAddress address;

	public McWsSServer(InetSocketAddress address) {
		super(address);
		this.address = address;
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
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		if (McWebsocketServerConfig.INSTANCE.debug.value()) {
			LOGGER.info("Received Message: " + message);
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
}
