package no.entur.android.nfc.websocket.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class NfcMessage {

    protected static final AtomicInteger ID_GENERATOR = new AtomicInteger(1);

    public static int nextId() {
        return ID_GENERATOR.incrementAndGet();
    }

    protected final int type;
    protected int id;

    public NfcMessage(int type, int id) {
        this.type = type;
        this.id = id;
    }

    public NfcMessage(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public void write(DataOutputStream dout) throws IOException {
    }

    public void read(DataInputStream din) throws IOException {
    }

    public void setId(int id) {
        this.id = id;
    }
}
