package no.entur.android.nfc.websocket.messages;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RequestResponseMessages implements NfcMessageListener {
	
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
		RequestResponseMessageLock lock = inflight.remove(message);
		if(lock != null) {
			lock.onMessage(message);
		} else {
			local.onMessage(message);
		}
	}
	
}
