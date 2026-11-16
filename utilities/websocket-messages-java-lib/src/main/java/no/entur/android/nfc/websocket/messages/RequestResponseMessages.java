package no.entur.android.nfc.websocket.messages;

import java.io.Closeable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RequestResponseMessages implements NfcMessageListener, Closeable {
	
	private final NfcMessageListener local;
	private final NfcMessageListener remote;
	
	private Map<Integer, RequestResponseMessageLock> inflight = new ConcurrentHashMap<>();

	public RequestResponseMessages(NfcMessageListener local, NfcMessageListener remote) {
		this.local = local;
		this.remote = remote;
	}

	public void send(NfcMessage message) {
		remote.onMessage(message);
	}
	
	public NfcMessage sendAndWaitForResponse(NfcMessage message, long duration) {
		
		RequestResponseMessageLock lock = new RequestResponseMessageLock(message);
		inflight.put(message.getId(), lock);
		
		remote.onMessage(message);
		try {
			return lock.waitForMessage(duration);
		} finally {
			inflight.remove(message.getId());
		}
	}
	
	@Override
	public void onMessage(NfcMessage message) {
		// Only responses can complete an in-flight request. Broadcasts (card present / lost, reader disconnected)
		// get their ids from the server's own id sequence, so they can collide with the ids of our requests.
		RequestResponseMessageLock lock = message instanceof NfcStatusResponseMessage ? inflight.remove(message.getId()) : null;
		if(lock != null) {
			// i.e. request-response
			lock.onMessage(message);
		} else {
			// i.e. broadcast or very delayed message
			local.onMessage(message);
		}
	}

	public void close() {
		for (Map.Entry<Integer, RequestResponseMessageLock> entry : inflight.entrySet()) {
			entry.getValue().close();
		}

	}
	
}
