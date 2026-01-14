package no.entur.android.nfc.mqtt.messages.sync;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SynchronizedRequestResponseMessages<T> implements SynchronizedResponseMessageListener<T>, Closeable {
	
	private SynchronizedRequestMessageListener<T> synchronizedRequestMessageListener;
	
	private Map<T, SynchronizedRequestResponseMessageLock<T>> inflight = new ConcurrentHashMap<>();

	public SynchronizedResponseMessage<T> sendAndWaitForResponse(SynchronizedRequestMessageRequest<T> message, long duration) throws IOException {

		// TODO should duration include time to send message?
		SynchronizedRequestResponseMessageLock<T> lock = new SynchronizedRequestResponseMessageLock<>(message);
		inflight.put(message.getCorrelationId(), lock);
		
		try {
			synchronizedRequestMessageListener.onRequestMessage(message, this);

			return lock.waitForMessage(duration);
		} finally {
			inflight.remove(message.getCorrelationId());
		}
	}
	
	@Override
	public void onResponseMessage(SynchronizedResponseMessage<T> message) {
		SynchronizedRequestResponseMessageLock<T> lock = inflight.remove(message.getCorrelationId());
		if(lock != null) {
			// i.e. request-response
			lock.onResponseMessage(message);
		}
	}

	public void close() {
		for (Map.Entry<T, SynchronizedRequestResponseMessageLock<T>> entry : inflight.entrySet()) {
			SynchronizedRequestResponseMessageLock value = entry.getValue();
			synchronized (value) {
				value.notifyAll();
			}
		}
	}

	public void setRequestMessageListener(SynchronizedRequestMessageListener<T> synchronizedRequestMessageListener) {
		this.synchronizedRequestMessageListener = synchronizedRequestMessageListener;
	}

    public void send(SynchronizedRequestMessageRequest<T> message) throws IOException {
        synchronizedRequestMessageListener.onRequestMessage(message, null);
    }

}
