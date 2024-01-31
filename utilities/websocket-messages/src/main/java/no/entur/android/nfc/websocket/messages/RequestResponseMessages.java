package no.entur.android.nfc.websocket.messages;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RequestResponseMessages implements NfcMessageListener {
	
	private final NfcMessageListener in;
	private final NfcMessageListener out;
	
	private Map<Integer, RequestResponseMessageLock> inflight = new ConcurrentHashMap<>();

	public RequestResponseMessages(NfcMessageListener delegate, NfcMessageListener out) {
		this.in = delegate;
		this.out = out;
	}
	
	public NfcMessage sendAndWaitForResponse(NfcMessage message, long duration) {
		
		RequestResponseMessageLock lock = new RequestResponseMessageLock(message);
		inflight.put(message.getId(), lock);
		
		out.onMessage(message);
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
			in.onMessage(message);
		}
	}
	
}
