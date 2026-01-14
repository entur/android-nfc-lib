package no.entur.android.nfc.mqtt.messages.reader;

import java.io.IOException;

import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestResponseMessages;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedResponseMessage;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedResponseMessageTimeoutException;

public class ReaderCommands<T, C extends ReaderContext> {

	protected final SynchronizedRequestResponseMessages readerExchange;

	protected C readerContext;

	public ReaderCommands(C readerContext, SynchronizedRequestResponseMessages readerExchange) {
		this.readerContext = readerContext;
		this.readerExchange = readerExchange;
	}

}
