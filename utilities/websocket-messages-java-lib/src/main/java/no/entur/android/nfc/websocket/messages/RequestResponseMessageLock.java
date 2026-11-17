package no.entur.android.nfc.websocket.messages;

public class RequestResponseMessageLock {

	private NfcMessage request;

	private volatile NfcMessage response;

	private volatile boolean closed;

	public RequestResponseMessageLock(NfcMessage request) {
		this.request = request;
	}

	public NfcMessage getRequest() {
		return request;
	}

	public void onMessage(NfcMessage response) {
		synchronized(this) {
			this.response = response;

			notifyAll();
		}
	}

	public void close() {
		synchronized(this) {
			this.closed = true;

			notifyAll();
		}
	}

	public NfcMessage waitForMessage(long duration) {
		try {
			synchronized(this) {
				if(duration <= 0) {
					// same as Object.wait(0): no timeout
					while(response == null && !closed) {
						wait();
					}
				} else {
					// loop to guard against spurious wakeups
					long deadline = System.nanoTime() + duration * 1_000_000L;
					while(response == null && !closed) {
						long remaining = (deadline - System.nanoTime()) / 1_000_000L;
						if(remaining <= 0) {
							break;
						}
						wait(remaining);
					}
				}
			}
		} catch (InterruptedException e) {
			return null;
		}
		return response;
	}
}
