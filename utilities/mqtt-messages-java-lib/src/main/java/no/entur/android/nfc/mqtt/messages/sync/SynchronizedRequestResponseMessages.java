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
			// only remove this request, not a later request with the same correlation id
			inflight.remove(message.getCorrelationId(), lock);
		}
	}

	@Override
	public void onResponseMessage(SynchronizedResponseMessage<T> message) {
		T correlationId = message.getCorrelationId();

		SynchronizedRequestResponseMessageLock<T> lock = inflight.get(correlationId);
		if(lock == null) {
			// no request waiting, i.e. a response which arrived after the request timed out
			return;
		}
		if(!lock.getRequest().isResponse(message)) {
			// i.e. a late response to an earlier request with the same correlation id
			return;
		}
		if(inflight.remove(correlationId, lock)) {
			// i.e. request-response
			lock.onResponseMessage(message);
		}
	}

	public void close() {
		for (Map.Entry<T, SynchronizedRequestResponseMessageLock<T>> entry : inflight.entrySet()) {
			entry.getValue().close();
		}
	}

	public void setRequestMessageListener(SynchronizedRequestMessageListener<T> synchronizedRequestMessageListener) {
		this.synchronizedRequestMessageListener = synchronizedRequestMessageListener;
	}

    public void send(SynchronizedRequestMessageRequest<T> message) throws IOException {
        synchronizedRequestMessageListener.onRequestMessage(message, null);
    }

}
