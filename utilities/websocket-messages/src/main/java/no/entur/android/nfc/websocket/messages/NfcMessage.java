package no.entur.android.nfc.websocket.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class NfcMessage {

    protected final int type;

    public NfcMessage(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void write(DataOutputStream dout) throws IOException {
    }

    public void read(DataInputStream din) throws IOException {
    }
}
