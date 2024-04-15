package info.rusty.mc.mcwebsocketserver;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class McWsServerBroadcastScheduler {
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
		broadcastJob = Executors.newScheduledThreadPool(1);

		Runnable broadcastTask = broadcastTask();

		broadcastJob.scheduleAtFixedRate(broadcastTask, 10, delay, TimeUnit.SECONDS);
	}

	private Runnable broadcastTask() {
		return () -> {
			wsServer.publicBroadcast();
			wsServer.subscriberBroadcast();
		};
	}
}
