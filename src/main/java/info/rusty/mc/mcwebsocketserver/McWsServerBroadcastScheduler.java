package info.rusty.mc.mcwebsocketserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class McWsServerBroadcastScheduler {
	public static final Logger LOGGER = LoggerFactory.getLogger("McWebsocketServer");
	private final McWsServer wsServer;
	private final int delay;
	private ScheduledExecutorService broadcastJob;

	public McWsServerBroadcastScheduler (McWsServer wsServerContext, int delay) {
		this.wsServer = wsServerContext;
		this.delay = delay;
		try {
			init();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void shutdown() {
		broadcastJob.shutdown();
	}

	public void init() throws InterruptedException {
		if (McWebsocketServerConfig.INSTANCE.debug.value()) {
			LOGGER.info("Starting broadcast scheduler with delay of " + delay + " seconds");
		}

		broadcastJob = Executors.newScheduledThreadPool(1);

		Runnable broadcastTask = broadcastTask();

		broadcastJob.scheduleAtFixedRate(broadcastTask, 10, delay, TimeUnit.SECONDS);
	}

	private Runnable broadcastTask() {
		return () -> {
			if (McWebsocketServerConfig.INSTANCE.debug.value()) {
				LOGGER.info("Broadcast Task running");
			}
			// Broadcast modules that don't rely on events:
			wsServer.subscriberBroadcast("player_positions");
		};
	}
}
