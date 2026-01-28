package no.entur.android.nfc.websocket.messages;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import no.entur.android.nfc.websocket.messages.card.CardApduRequestMessage;
import no.entur.android.nfc.websocket.messages.card.CardApduResponseMessage;
import no.entur.android.nfc.websocket.messages.card.broadcast.CardLostMessage;
import no.entur.android.nfc.websocket.messages.card.broadcast.CardPresentMessage;
import no.entur.android.nfc.websocket.messages.reader.ReaderBeginPollingRequestMessage;
import no.entur.android.nfc.websocket.messages.reader.ReaderBeginPollingResponseMessage;
import no.entur.android.nfc.websocket.messages.reader.ReaderConnectRequestMessage;
import no.entur.android.nfc.websocket.messages.reader.ReaderConnectResponseMessage;
import no.entur.android.nfc.websocket.messages.reader.ReaderDisconnectRequestMessage;
import no.entur.android.nfc.websocket.messages.reader.ReaderDisconnectResponseMessage;
import no.entur.android.nfc.websocket.messages.reader.ReaderEndPollingRequestMessage;
import no.entur.android.nfc.websocket.messages.reader.ReaderEndPollingResponseMessage;
import no.entur.android.nfc.websocket.messages.reader.broadcast.ReaderDisconnectedMessage;

public class NfcMessageReader {

    public static final int STATUS_OK = 0;
    public static final int VERSION = 1;

    public NfcMessage parse(byte[] input) {
        try {
            DataInputStream din = new DataInputStream(new ByteArrayInputStream(input));

            int version = din.readInt();
            if (version == VERSION) {
                int type = din.readInt();
                int id = din.readInt();

                NfcMessage message = getMessage(type, id);

                message.read(din);

                return message;
            } else {
                throw new RuntimeException("Unexpected version " + version);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to parse message");
        }
    }

    private NfcMessage getMessage(int type, int id) {

        switch (type) {
            // reader
            case ReaderConnectRequestMessage.TYPE: return new ReaderConnectRequestMessage(id);
            case ReaderConnectResponseMessage.TYPE: return new ReaderConnectResponseMessage(id);

            case ReaderDisconnectRequestMessage.TYPE: return new ReaderDisconnectRequestMessage(id);
            case ReaderDisconnectResponseMessage.TYPE: return new ReaderDisconnectResponseMessage(id);

            case ReaderBeginPollingRequestMessage.TYPE: return new ReaderBeginPollingRequestMessage(id);
            case ReaderBeginPollingResponseMessage.TYPE: return new ReaderBeginPollingResponseMessage(id);

            case ReaderEndPollingRequestMessage.TYPE: return new ReaderEndPollingRequestMessage(id);
            case ReaderEndPollingResponseMessage.TYPE: return new ReaderEndPollingResponseMessage(id);

            // broadcast
            case ReaderDisconnectedMessage.TYPE: return new ReaderDisconnectedMessage(id);

            // card
            case CardApduRequestMessage.TYPE: return new CardApduRequestMessage(id);
            case CardApduResponseMessage.TYPE: return new CardApduResponseMessage(id);

            // broadcast
            case CardLostMessage.TYPE: return new CardLostMessage(id);
            case CardPresentMessage.TYPE: return new CardPresentMessage(id);

            default: throw new IllegalArgumentException("Unknown type " + type);
        }
    }
}
