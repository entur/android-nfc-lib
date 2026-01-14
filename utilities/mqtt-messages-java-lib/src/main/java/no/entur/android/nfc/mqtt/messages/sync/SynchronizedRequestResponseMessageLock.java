package no.entur.android.nfc.mqtt.messages.sync;

import java.io.IOException;

public class SynchronizedRequestResponseMessageLock<T> {

	private SynchronizedRequestMessageRequest<T> request;
	
	private volatile SynchronizedResponseMessage<T> response;
	
	public SynchronizedRequestResponseMessageLock(SynchronizedRequestMessageRequest<T> request) {
		this.request = request;
	}
	
	public SynchronizedRequestMessageRequest<T> getRequest() {
		return request;
	}
	
	public void onResponseMessage(SynchronizedResponseMessage<T> response) {
		this.response = response;
		
		synchronized(this) {
			notifyAll();
		}
	}
	
	public SynchronizedResponseMessage<T> waitForMessage(long duration) throws IOException {
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
			throw new SynchronizedResponseMessageTimeoutException();
		}
		return response;
	}
}
