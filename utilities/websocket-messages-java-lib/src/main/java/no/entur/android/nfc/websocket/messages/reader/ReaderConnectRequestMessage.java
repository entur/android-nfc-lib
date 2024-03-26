package no.entur.android.nfc.websocket.messages.reader;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import no.entur.android.nfc.websocket.messages.NfcMessage;

public class ReaderConnectRequestMessage extends NfcMessage {

    public static final int TYPE = 2;

    private List<String> tags;

    public ReaderConnectRequestMessage(List<String> tags) {
        this(nextId());
        this.tags = tags;
    }

    public ReaderConnectRequestMessage(int id) {
        super(TYPE, id);
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getTags() {
        return tags;
    }

    @Override
    public void write(DataOutputStream dout) throws IOException {
        super.write(dout);

        dout.writeInt(tags.size());
        for (String technology : tags) {
            dout.writeUTF(technology);
        }
    }

    @Override
    public void read(DataInputStream din) throws IOException {
        super.read(din);

        int count = din.readInt();
        List<String> tags = new ArrayList<>();
        for(int i = 0; i < count; i++) {
            tags.add(din.readUTF());
        }

        this.tags = tags;
    }
}
