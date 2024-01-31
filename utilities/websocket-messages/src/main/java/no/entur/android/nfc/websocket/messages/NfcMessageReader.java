package no.entur.android.nfc.websocket.messages;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

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
            case ReaderConnectMessage.TYPE: return new ReaderConnectMessage();
            case ReaderConnectedMessage.TYPE: return new ReaderConnectedMessage();
        }

        throw new IllegalArgumentException("Unknown type " + type);
    }
}
