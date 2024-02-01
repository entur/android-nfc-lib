package no.entur.android.nfc.websocket.messages.card.broadcast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import no.entur.android.nfc.websocket.messages.NfcMessage;

public class CardPresentMessage extends NfcMessage {

    public static final int TYPE = 101;

    private List<String> technologies;

    public CardPresentMessage(List<String> technologies) {
        this.technologies = technologies;
    }

    public CardPresentMessage() {
    }

    @Override
    public void write(DataOutputStream dout) throws IOException {
        super.write(dout);
        dout.writeInt(technologies.size());
        for (String technology : technologies) {
            dout.writeUTF(technology);
        }
    }

    @Override
    public void read(DataInputStream din) throws IOException {
        super.read(din);

        int count = din.readInt();
        List<String> technologies = new ArrayList<>();
        for(int i = 0; i < count; i++) {
            technologies.add(din.readUTF());
        }

        this.technologies = technologies;
    }

    public List<String> getTechnologies() {
        return technologies;
    }
}
