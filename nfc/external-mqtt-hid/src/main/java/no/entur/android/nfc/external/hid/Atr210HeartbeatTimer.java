package no.entur.android.nfc.external.hid;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Atr210HeartbeatTimer {

    protected final long intervalInMillis;
    protected final ScheduledExecutorService executorService;
    protected final Atr210MqttHandler listener;

    protected Future<?> scheduledTask = CompletableFuture.completedFuture(null);

	public Atr210HeartbeatTimer(long intervalInMillis, Atr210MqttHandler service) {
		this(intervalInMillis, Executors.newSingleThreadScheduledExecutor(), service);
	}

	public Atr210HeartbeatTimer(long intervalInMillis, ScheduledExecutorService executorService, Atr210MqttHandler service) {
		this.intervalInMillis = intervalInMillis;
		this.executorService = executorService;
		this.listener = service;
	}

	public void cancel() {
		scheduledTask.cancel(false);
	}

	public void schedule() {
		cancel();

		scheduledTask = executorService.schedule(() -> timeout(), intervalInMillis + 1, TimeUnit.MILLISECONDS);
	}

	private void timeout() {
		if(!listener.verifyHeartbeats()) {
            cancel();
        }
	}

    public void close() {
        cancel();

        executorService.shutdownNow();
    }
}
