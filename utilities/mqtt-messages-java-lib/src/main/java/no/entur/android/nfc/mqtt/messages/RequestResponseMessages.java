package no.entur.android.nfc.mqtt.messages;

import java.io.Closeable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RequestResponseMessages<T> implements RequestResponseMessageListener<T>, Closeable {
	
	private final RequestResponseMessageListener<T> remote;
	
	private Map<T, RequestResponseMessageLock<T>> inflight = new ConcurrentHashMap<>();

	public RequestResponseMessages(RequestResponseMessageListener<T> remote) {
		this.remote = remote;
	}

	public void send(RequestResponseMessage<T> message) {
		remote.onMessage(message);
	}
	
	public RequestResponseMessage<T> sendAndWaitForResponse(RequestResponseMessage<T> message, long duration) {
		
		RequestResponseMessageLock<T> lock = new RequestResponseMessageLock<>(message);
		inflight.put(message.getCorrelationId(), lock);
		
		remote.onMessage(message);
		try {
			return lock.waitForMessage(duration);
		} finally {
			inflight.remove(message.getCorrelationId());
		}
	}
	
	@Override
	public void onMessage(RequestResponseMessage<T> message) {
		RequestResponseMessageLock<T> lock = inflight.remove(message.getCorrelationId());
		if(lock != null) {
			// i.e. request-response
			lock.onMessage(message);
		}
	}

	public void close() {
		for (Map.Entry<T, RequestResponseMessageLock<T>> entry : inflight.entrySet()) {
			RequestResponseMessageLock value = entry.getValue();
			synchronized (value) {
				value.notifyAll();
			}
		}
	}
	
}
