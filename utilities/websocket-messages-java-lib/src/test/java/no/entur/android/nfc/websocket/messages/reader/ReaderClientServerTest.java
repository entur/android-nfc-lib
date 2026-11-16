package no.entur.android.nfc.websocket.messages.reader;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import no.entur.android.nfc.websocket.messages.NfcMessageReader;
import no.entur.android.nfc.websocket.messages.NfcMessageWriter;
import no.entur.android.nfc.websocket.messages.RequestResponseMessages;

public class ReaderClientServerTest {

	private static class Listener implements ReaderServer.Listener {

		private final boolean result;

		private Listener(boolean result) {
			this.result = result;
		}

		@Override
		public boolean onConnect(List<String> tags) {
			return result;
		}

		@Override
		public boolean onDisconnect() {
			return result;
		}

		@Override
		public boolean onBeginPolling() {
			return result;
		}

		@Override
		public boolean onEndPolling() {
			return result;
		}
	}

	private ReaderClient createClient(boolean result) {
		NfcMessageWriter writer = new NfcMessageWriter();
		NfcMessageReader reader = new NfcMessageReader();

		AtomicReference<RequestResponseMessages> client = new AtomicReference<>();

		// serialize in both directions, like over the web socket
		ReaderServer server = new ReaderServer((m) -> client.get().onMessage(reader.parse(writer.write(m))));
		server.setListener(new Listener(result));

		client.set(new RequestResponseMessages((m) -> {}, (m) -> server.onMessage(reader.parse(writer.write(m)))));

		return new ReaderClient(client.get(), 1000);
	}

	@Test
	public void testConnectAndBeginPolling() {
		ReaderClient client = createClient(true);

		assertTrue(client.connect(new String[]{}));
		assertTrue(client.beginPolling());
	}

	@Test
	public void testConnectFailureIsReported() {
		ReaderClient client = createClient(false);

		assertFalse(client.connect(new String[]{}));
	}

	@Test
	public void testBeginPollingFailureIsReported() {
		ReaderClient client = createClient(false);

		assertFalse(client.beginPolling());
	}

}
