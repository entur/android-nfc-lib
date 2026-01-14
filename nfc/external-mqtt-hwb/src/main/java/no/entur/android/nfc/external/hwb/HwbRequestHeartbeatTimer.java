package no.entur.android.nfc.external.hwb;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HwbRequestHeartbeatTimer {

    protected final long intervalInMillis;
    protected final ScheduledExecutorService executorService;
    protected final HwbMqttHandler listener;

    protected Future<?> scheduledTask = CompletableFuture.completedFuture(null);

	public HwbRequestHeartbeatTimer(long intervalInMillis, HwbMqttHandler service) {
		this(intervalInMillis, Executors.newSingleThreadScheduledExecutor(), service);
	}

	public HwbRequestHeartbeatTimer(long intervalInMillis, ScheduledExecutorService executorService, HwbMqttHandler service) {
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
		listener.discoverReaders();
	}

    public void close() {
        cancel();

        executorService.shutdownNow();
    }

    public long getIntervalInMillis() {
        return intervalInMillis;
    }
}
