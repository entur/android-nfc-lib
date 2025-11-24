package no.entur.android.nfc.mqtt.messages.reader;

import java.io.IOException;

import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestResponseMessages;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedResponseMessage;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedResponseMessageTimeoutException;

public class ReaderCommands<T, C extends ReaderContext> {

	protected final SynchronizedRequestResponseMessages readerExchange;

	protected long readerPresentTimeout;

	protected C readerContext;

	protected ReaderPresentMessageConverter<T, C> readerPresentMessageConverter;

	public ReaderCommands(C readerContext, SynchronizedRequestResponseMessages readerExchange, long readerPresentTimeout, ReaderPresentMessageConverter<T, C> readerPresentMessageConverter) {
		this.readerContext = readerContext;
		this.readerExchange = readerExchange;
		this.readerPresentTimeout = readerPresentTimeout;
		this.readerPresentMessageConverter = readerPresentMessageConverter;
	}

	public boolean isPresent() throws IOException  {
		ReaderPresentSynchronizedRequestMessageRequest<T, ?> request = readerPresentMessageConverter.createReaderPresentRequestMessage(readerContext);

		try {
			SynchronizedResponseMessage<T> response = readerExchange.sendAndWaitForResponse(request, readerPresentTimeout);

			if (response != null) {
				ReaderPresentResponseMessage result = readerPresentMessageConverter.createReaderPresentResponseMessage(response, readerContext);
				return result.isPresent();
			}
			throw new IOException();
		} catch (SynchronizedResponseMessageTimeoutException e) {
			// timeout; not present
			return false;
		}
	}


}
