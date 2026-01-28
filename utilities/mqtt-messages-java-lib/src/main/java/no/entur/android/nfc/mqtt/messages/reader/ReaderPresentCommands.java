package no.entur.android.nfc.mqtt.messages.reader;

import java.io.IOException;

import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestResponseMessages;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedResponseMessage;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedResponseMessageTimeoutException;

public class ReaderPresentCommands<T, C extends ReaderContext> extends ReaderCommands<T, C> {

	protected ReaderPresentMessageConverter<T, C> readerPresentMessageConverter;

	public ReaderPresentCommands(C readerContext, SynchronizedRequestResponseMessages readerExchange, ReaderPresentMessageConverter<T, C> readerPresentMessageConverter) {
        super(readerContext, readerExchange);
		this.readerPresentMessageConverter = readerPresentMessageConverter;
	}

	public boolean isPresent(long readerPresentTimeout) throws IOException  {
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
