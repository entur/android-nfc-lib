package no.entur.android.nfc.websocket.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class NfcMessage {

    protected int id;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void write(DataOutputStream dout) throws IOException {
        dout.writeInt(id);
    }

    public void read(DataInputStream din) throws IOException {
        id = din.readInt();
    }
}
