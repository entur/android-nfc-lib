package no.entur.android.nfc.external.hid.test.heartbeat;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HeartbeatTimer {

    public interface HeartbeatListener {
        void onHeartbeat();

    }

	private final long intervalInMillis;
	private final ScheduledExecutorService executorService;
	private final HeartbeatListener listener;

	private Future<?> scheduledTask = CompletableFuture.completedFuture(null);

	public HeartbeatTimer(long intervalInMillis, HeartbeatListener service) {
		this(intervalInMillis, Executors.newSingleThreadScheduledExecutor(), service);
	}

	public HeartbeatTimer(long intervalInMillis, ScheduledExecutorService executorService, HeartbeatListener service) {
		this.intervalInMillis = intervalInMillis;
		this.executorService = executorService;
		this.listener = service;
	}

	public void cancel() {
		scheduledTask.cancel(false);
	}

	public void schedule() {
		cancel();

		scheduledTask = executorService.scheduleWithFixedDelay(() -> timeout(), 1, intervalInMillis + 1, TimeUnit.MILLISECONDS);
	}

	private void timeout() {
		listener.onHeartbeat();
	}

    public void close() {
        cancel();

        executorService.shutdownNow();
    }
}
