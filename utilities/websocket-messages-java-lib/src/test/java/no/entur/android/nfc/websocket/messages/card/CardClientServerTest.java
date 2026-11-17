package no.entur.android.nfc.websocket.messages.card;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import no.entur.android.nfc.websocket.messages.NfcMessageReader;
import no.entur.android.nfc.websocket.messages.NfcMessageWriter;
import no.entur.android.nfc.websocket.messages.RequestResponseMessages;

public class CardClientServerTest {

	private CardClient createClient(CardServer.Listener listener) {
		NfcMessageWriter writer = new NfcMessageWriter();
		NfcMessageReader reader = new NfcMessageReader();

		AtomicReference<RequestResponseMessages> client = new AtomicReference<>();

		// serialize in both directions, like over the web socket
		CardServer server = new CardServer((m) -> client.get().onMessage(reader.parse(writer.write(m))));
		server.setListener(listener);

		client.set(new RequestResponseMessages((m) -> {}, (m) -> server.onMessage(reader.parse(writer.write(m)))));

		return new CardClient(client.get(), 1000);
	}

	@Test
	public void testTransceive() throws IOException {
		CardClient client = createClient((command) -> new byte[]{0x01, 0x02});

		assertArrayEquals(new byte[]{0x01, 0x02}, client.transcieveOrThrow(new byte[]{0x30, 0x00}));
		assertArrayEquals(new byte[]{0x01, 0x02}, client.transcieve(new byte[]{0x30, 0x00}));
	}

	@Test
	public void testCardLost() {
		CardClient client = createClient((command) -> {
			throw new CardLostException("No response to GET_VERSION");
		});

		assertThrows(CardLostException.class, () -> client.transcieveOrThrow(new byte[]{0x60}));
		assertNull(client.transcieve(new byte[]{0x60}));
	}

	@Test
	public void testUnableToTransceive() {
		CardClient client = createClient((command) -> {
			throw new IOException("Unsupported command");
		});

		IOException e = assertThrows(IOException.class, () -> client.transcieveOrThrow(new byte[]{0x1B}));
		assertFalse(e instanceof CardLostException);
		assertNull(client.transcieve(new byte[]{0x1B}));
	}

}
