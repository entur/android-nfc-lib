package no.entur.android.nfc.external.hwb;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HwbVerifyHeartbeatTimer {

    protected final long intervalInMillis;
    protected final ScheduledExecutorService executorService;
    protected final HwbMqttHandler listener;

    protected Future<?> scheduledTask = CompletableFuture.completedFuture(null);

	public HwbVerifyHeartbeatTimer(long intervalInMillis, HwbMqttHandler service) {
		this(intervalInMillis, Executors.newSingleThreadScheduledExecutor(), service);
	}

	public HwbVerifyHeartbeatTimer(long intervalInMillis, ScheduledExecutorService executorService, HwbMqttHandler service) {
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

    public long getIntervalInMillis() {
        return intervalInMillis;
    }
}
