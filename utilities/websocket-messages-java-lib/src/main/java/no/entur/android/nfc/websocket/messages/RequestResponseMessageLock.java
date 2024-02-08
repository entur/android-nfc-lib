package no.entur.android.nfc.websocket.messages;

public class RequestResponseMessageLock {

	private NfcMessage request;
	
	private volatile NfcMessage response;
	
	public RequestResponseMessageLock(NfcMessage request) {
		this.request = request;
	}
	
	public NfcMessage getRequest() {
		return request;
	}
	
	public void onMessage(NfcMessage response) {
		this.response = response;
		
		synchronized(this) {
			notifyAll();
		}
	}
	
	public NfcMessage waitForMessage(long duration) {
		try {
			synchronized(this) {
				if(response == null) {
					wait(duration);
				}
			}
		} catch (InterruptedException e) {
			return null;
		}
		return response;
	}
}
