package no.entur.android.nfc.mqtt.messages;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RequestResponseMessages<T> implements ResponseMessageListener<T>, Closeable {
	
	private final RequestMessageListener<T> remote;
	
	private Map<T, RequestResponseMessageLock<T>> inflight = new ConcurrentHashMap<>();

	public RequestResponseMessages(RequestMessageListener<T> remote) {
		this.remote = remote;
	}

	public ResponseMessage<T> sendAndWaitForResponse(RequestMessage<T> message, long duration) throws IOException {

		// TODO should duration include time to send message?
		RequestResponseMessageLock<T> lock = new RequestResponseMessageLock<>(message);
		inflight.put(message.getCorrelationId(), lock);
		
		remote.onRequestMessage(message, this);
		try {
			return lock.waitForMessage(duration);
		} finally {
			inflight.remove(message.getCorrelationId());
		}
	}
	
	@Override
	public void onResponseMessage(ResponseMessage<T> message) {
		RequestResponseMessageLock<T> lock = inflight.remove(message.getCorrelationId());
		if(lock != null) {
			// i.e. request-response
			lock.onResponseMessage(message);
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
