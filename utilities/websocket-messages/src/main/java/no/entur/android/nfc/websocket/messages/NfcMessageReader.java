package no.entur.android.nfc.websocket.messages;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import no.entur.android.nfc.websocket.messages.card.CardAdpuRequestMessage;
import no.entur.android.nfc.websocket.messages.card.CardAdpuResponseMessage;
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

                NfcMessage message = getMessage(type);

                message.read(din);

                return message;
            } else {
                throw new RuntimeException("Unexpected version " + version);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to parse message");
        }
    }

    private NfcMessage getMessage(int type) {

        switch (type) {
            // reader
            case ReaderConnectRequestMessage.TYPE: return new ReaderConnectRequestMessage();
            case ReaderConnectResponseMessage.TYPE: return new ReaderConnectResponseMessage();

            case ReaderDisconnectRequestMessage.TYPE: return new ReaderDisconnectRequestMessage();
            case ReaderDisconnectResponseMessage.TYPE: return new ReaderDisconnectResponseMessage();

            case ReaderBeginPollingRequestMessage.TYPE: return new ReaderBeginPollingRequestMessage();
            case ReaderBeginPollingResponseMessage.TYPE: return new ReaderBeginPollingResponseMessage();

            case ReaderEndPollingRequestMessage.TYPE: return new ReaderEndPollingRequestMessage();
            case ReaderEndPollingResponseMessage.TYPE: return new ReaderEndPollingResponseMessage();

            // broadcast
            case ReaderDisconnectedMessage.TYPE: return new ReaderDisconnectedMessage();

            // card
            case CardAdpuRequestMessage.TYPE: return new CardAdpuRequestMessage();
            case CardAdpuResponseMessage.TYPE: return new CardAdpuResponseMessage();

            // broadcast
            case CardLostMessage.TYPE: return new CardLostMessage();
            case CardPresentMessage.TYPE: return new CardPresentMessage();
        }

        throw new IllegalArgumentException("Unknown type " + type);
    }
}
