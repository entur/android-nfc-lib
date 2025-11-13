package no.entur.android.nfc.mqtt.messages;

public class RequestResponseMessageLock<T> {

	private RequestResponseMessage<T> request;
	
	private volatile RequestResponseMessage<T> response;
	
	public RequestResponseMessageLock(RequestResponseMessage<T> request) {
		this.request = request;
	}
	
	public RequestResponseMessage<T> getRequest() {
		return request;
	}
	
	public void onMessage(RequestResponseMessage<T> response) {
		this.response = response;
		
		synchronized(this) {
			notifyAll();
		}
	}
	
	public RequestResponseMessage<T> waitForMessage(long duration) {
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
