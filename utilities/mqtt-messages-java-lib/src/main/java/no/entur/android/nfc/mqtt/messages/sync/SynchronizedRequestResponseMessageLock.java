package no.entur.android.nfc.mqtt.messages.sync;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class SynchronizedRequestResponseMessageLock<T> {

	private SynchronizedRequestMessageRequest<T> request;

	private volatile SynchronizedResponseMessage<T> response;

	private volatile boolean closed;

	public SynchronizedRequestResponseMessageLock(SynchronizedRequestMessageRequest<T> request) {
		this.request = request;
	}

	public SynchronizedRequestMessageRequest<T> getRequest() {
		return request;
	}

	public void onResponseMessage(SynchronizedResponseMessage<T> response) {
		synchronized(this) {
			this.response = response;

			notifyAll();
		}
	}

	/**
	 * Stop waiting for a response.
	 */

	public void close() {
		synchronized(this) {
			this.closed = true;

			notifyAll();
		}
	}

	/**
	 * Wait for the response.
	 *
	 * @param duration timeout in milliseconds, 0 or less for no timeout (as for {@link Object#wait(long)})
	 * @return the response
	 * @throws IOException if closed or interrupted, or {@link SynchronizedResponseMessageTimeoutException} on timeout
	 */

	public SynchronizedResponseMessage<T> waitForMessage(long duration) throws IOException {
		try {
			synchronized(this) {
				// loop to guard against spurious wakeups
				long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(duration);
				while(response == null && !closed) {
					if(duration <= 0) {
						wait();
					} else {
						long remaining = deadline - System.nanoTime();
						if(remaining <= 0) {
							break;
						}
						TimeUnit.NANOSECONDS.timedWait(this, remaining);
					}
				}
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();

			throw new IOException("Interrupted waiting for response", e);
		}
		SynchronizedResponseMessage<T> response = this.response;
		if(response == null) {
			if(closed) {
				throw new IOException("Closed while waiting for response");
			}
			throw new SynchronizedResponseMessageTimeoutException();
		}
		return response;
	}
}
