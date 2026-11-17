package no.entur.android.nfc.websocket.server;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import no.entur.android.nfc.websocket.messages.NfcMessage;
import no.entur.android.nfc.websocket.messages.card.CardLostException;
import no.entur.android.nfc.websocket.messages.card.broadcast.CardLostMessage;
import no.entur.android.nfc.websocket.messages.card.broadcast.CardPresentMessage;

public class WebSocketNfcServerTest {

    // MIFARE Ultralight, so that the version and signature are read (which reconnects the card)
    private static final byte[] ULTRALIGHT_ATR = fromHex("3B8F8001804F0CA0000003060300030000000068");

    private static final byte[] UID = fromHex("04112233445566");

    /**
     * Card with a channel which returns the UID.
     */

    private static class FakeChannelCard extends FakeCard {

        @Override
        public ATR getATR() {
            return new ATR(ULTRALIGHT_ATR);
        }

        @Override
        public CardChannel getBasicChannel() {
            Card card = this;
            return new CardChannel() {
                @Override
                public Card getCard() {
                    return card;
                }

                @Override
                public int getChannelNumber() {
                    return 0;
                }

                @Override
                public ResponseAPDU transmit(CommandAPDU command) {
                    byte[] response = Arrays.copyOf(UID, UID.length + 2);
                    response[UID.length] = (byte) 0x90;
                    return new ResponseAPDU(response);
                }

                @Override
                public int transmit(ByteBuffer command, ByteBuffer response) {
                    response.put((byte) 0x90).put((byte) 0x00);
                    return 2;
                }

                @Override
                public void close() {
                }
            };
        }
    }

    private static class FakeReader extends ExtendedCardTerminal {

        private final String name;

        private final List<FakeCard> connected = new CopyOnWriteArrayList<>();

        private volatile boolean polling;

        // GET_VERSION and READ_SIG responses, null if the card did not respond
        private volatile List<byte[]> transparentResponses = Arrays.asList(fromHex("0004040201001303"), new byte[32]);

        private FakeReader(String name, String tag) {
            super(null);
            this.name = name;
            setTags(Collections.singletonList(tag));
        }

        @Override
        public void startPolling() {
            polling = true;
        }

        @Override
        public void stopPolling() {
            polling = false;
        }

        @Override
        public List<String> identifyTechnologies(Card card, CardChannel channel) {
            return Collections.singletonList("MifareUltralight");
        }

        @Override
        public List<byte[]> transceiveTransparent(List<byte[]> commands) {
            // GET_VERSION and READ_SIG, which re-activates the card
            return transparentResponses;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Card connect(String protocol) {
            FakeCard card = new FakeChannelCard();
            connected.add(card);
            return card;
        }

        @Override
        public boolean isCardPresent() {
            return false;
        }

        @Override
        public boolean waitForCardPresent(long timeout) {
            return true;
        }

        @Override
        public boolean waitForCardAbsent(long timeout) {
            return true;
        }
    }

    private static WebSocketNfcServer server(FakeReader... readers) throws CardException {
        ExtendedCardTerminalFactory factory = new ExtendedCardTerminalFactory(terminal -> {}) {
            @Override
            protected ExtendedCardTerminal createTerminal(CardTerminal cardTerminal) {
                return (FakeReader) cardTerminal;
            }
        };
        WebSocketNfcServer server = new WebSocketNfcServer(0, new NoopCardTerminalsFilter(), factory);
        for (FakeReader reader : readers) {
            server.getCardTerminalsPollingPool().connected(reader);
        }
        return server;
    }

    @Test
    public void testReconnectedCardIsDisconnectedIfCardConnectedFails() throws CardException {
        FakeReader reader = new FakeReader("Reader", "a");
        WebSocketNfcServer server = server(reader);

        // i.e. the client connection was closed meanwhile
        WebSocketNfcServer.Attachment attachment = server.createAttachment(message -> {
            if(message instanceof CardPresentMessage) {
                throw new IllegalStateException("Not connected");
            }
        });
        assertTrue(attachment.onConnect(Collections.singletonList("a")));

        FakeCard card = new FakeChannelCard();
        assertThrows(IllegalStateException.class, () -> attachment.cardConnected(card));

        // the polling server disconnects the original card, so only the reconnected card
        assertEquals(1, reader.connected.size());
        assertTrue(reader.connected.get(0).isDisconnected());
        assertFalse(card.isDisconnected());

        // no card
        assertThrows(CardException.class, () -> attachment.transcieve(new byte[]{0x30, 0x04}));
    }

    @Test
    public void testCardWhichDidNotAnswerGetVersionIsLost() throws Exception {
        FakeReader reader = new FakeReader("Reader", "a");
        // i.e. original MIFARE Ultralight: no response to GET_VERSION and READ_SIG
        reader.transparentResponses = Arrays.asList(null, null);
        WebSocketNfcServer server = server(reader);

        List<NfcMessage> messages = new CopyOnWriteArrayList<>();
        WebSocketNfcServer.Attachment attachment = server.createAttachment(messages::add);
        assertTrue(attachment.onConnect(Collections.singletonList("a")));

        FakeCard card = new FakeChannelCard();
        attachment.cardConnected(card);

        // like Android's own NFC reader, the card is lost
        assertThrows(CardLostException.class, () -> attachment.transcieve(new byte[]{0x60}));
        assertEquals(1, count(messages, CardLostMessage.class));

        // until it is removed and presented again; removing it does not report it as lost again
        assertThrows(CardLostException.class, () -> attachment.transcieve(new byte[]{0x30, 0x04}));
        attachment.cardDisconnected(card);
        assertEquals(1, count(messages, CardLostMessage.class));

        FakeCard next = new FakeChannelCard();
        attachment.cardConnected(next);

        // the command reaches the card again (the fake card returns no data, so READ fails, but not as lost)
        IOException e = assertThrows(IOException.class, () -> attachment.transcieve(new byte[]{0x30, 0x04}));
        assertFalse(e instanceof CardLostException, e.toString());

        attachment.cardDisconnected(next);
        assertEquals(2, count(messages, CardLostMessage.class));
    }

    @Test
    public void testCardWhichAnsweredGetVersionIsNotLost() throws Exception {
        WebSocketNfcServer server = server(new FakeReader("Reader", "a"));

        List<NfcMessage> messages = new CopyOnWriteArrayList<>();
        WebSocketNfcServer.Attachment attachment = server.createAttachment(messages::add);
        assertTrue(attachment.onConnect(Collections.singletonList("a")));

        attachment.cardConnected(new FakeChannelCard());

        assertArrayEquals(fromHex("0004040201001303"), attachment.transcieve(new byte[]{0x60}));
        assertEquals(0, count(messages, CardLostMessage.class));
    }

    private static long count(List<NfcMessage> messages, Class<? extends NfcMessage> type) {
        return messages.stream().filter(type::isInstance).count();
    }

    @Test
    public void testTranscieveWithoutCard() throws CardException {
        WebSocketNfcServer server = server(new FakeReader("Reader", "a"));

        WebSocketNfcServer.Attachment attachment = server.createAttachment(message -> {});
        assertTrue(attachment.onConnect(Collections.singletonList("a")));

        assertThrows(CardException.class, () -> attachment.transcieve(new byte[]{0x30, 0x04}));
    }

    @Test
    public void testConnectToAnotherReaderReturnsTheFirstReader() throws CardException {
        FakeReader first = new FakeReader("First", "a");
        FakeReader second = new FakeReader("Second", "b");
        WebSocketNfcServer server = server(first, second);

        WebSocketNfcServer.Attachment attachment = server.createAttachment(message -> {});
        assertTrue(attachment.onConnect(Collections.singletonList("a")));
        assertTrue(attachment.onBeginPolling());
        assertTrue(first.polling);

        assertTrue(attachment.onConnect(Collections.singletonList("b")));

        // the first reader no longer polls, and can be borrowed again
        assertFalse(first.polling);
        WebSocketNfcServer.Attachment other = server.createAttachment(message -> {});
        assertTrue(other.onConnect(Collections.singletonList("a")));

        attachment.release();
        other.release();
    }

    private static byte[] fromHex(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return bytes;
    }
}
