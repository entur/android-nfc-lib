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
    private byte[] atr;
    private byte[] historicalBytes;
    private byte[] uid;

    public CardPresentMessage(List<String> technologies, byte[] atr, byte[] historicalBytes, byte[] uid) {
        this(nextId());
        this.technologies = technologies;
        this.atr = atr;
        this.historicalBytes = historicalBytes;
        this.uid = uid;
    }

    public CardPresentMessage(int id) {
        super(TYPE, id);
    }

    public CardPresentMessage() {
        super(TYPE, nextId());
    }

    @Override
    public void write(DataOutputStream dout) throws IOException {
        super.write(dout);

        dout.writeInt(atr.length);
        dout.write(atr);

        dout.writeInt(historicalBytes.length);
        dout.write(historicalBytes);

        dout.writeInt(uid.length);
        dout.write(uid);

        dout.writeInt(technologies.size());
        for (String technology : technologies) {
            dout.writeUTF(technology);
        }
    }

    @Override
    public void read(DataInputStream din) throws IOException {
        super.read(din);

        int atrLength = din.readInt();
        byte[] atr = new byte[atrLength];
        din.readFully(atr);
        this.atr = atr;

        int historicalBytesLength = din.readInt();
        byte[] historicalBytes = new byte[historicalBytesLength];
        din.readFully(historicalBytes);
        this.historicalBytes = historicalBytes;

        int uidLength = din.readInt();
        byte[] uid = new byte[uidLength];
        din.readFully(uid);
        this.uid = uid;

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

    public byte[] getAtr() {
        return atr;
    }

    public byte[] getHistoricalBytes() {
        return historicalBytes;
    }

    public byte[] getUid() {
        return uid;
    }

}
