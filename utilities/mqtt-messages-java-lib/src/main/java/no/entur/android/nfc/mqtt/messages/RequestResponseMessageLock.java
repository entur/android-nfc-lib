package no.entur.android.nfc.mqtt.messages;

import java.io.IOException;

public class RequestResponseMessageLock<T> {

	private RequestMessage<T> request;
	
	private volatile ResponseMessage<T> response;
	
	public RequestResponseMessageLock(RequestMessage<T> request) {
		this.request = request;
	}
	
	public RequestMessage<T> getRequest() {
		return request;
	}
	
	public void onResponseMessage(ResponseMessage<T> response) {
		this.response = response;
		
		synchronized(this) {
			notifyAll();
		}
	}
	
	public ResponseMessage<T> waitForMessage(long duration) throws IOException {
		try {
			synchronized(this) {
				if(response == null) {
					wait(duration);
				}
			}
		} catch (InterruptedException e) {
			throw new IOException("Interrupted waiting for response", e);
		}
		if(response == null) {
			throw new RequestResponseTimeoutIOException();
		}
		return response;
	}
}
