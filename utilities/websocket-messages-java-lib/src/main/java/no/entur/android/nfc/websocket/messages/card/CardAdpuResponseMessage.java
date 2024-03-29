package no.entur.android.nfc.websocket.messages.card;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import no.entur.android.nfc.websocket.messages.NfcMessage;
import no.entur.android.nfc.websocket.messages.NfcStatusResponseMessage;

public class CardAdpuResponseMessage extends NfcStatusResponseMessage {

    public static final int TYPE = 102;
    
    private byte[] adpu;

    public CardAdpuResponseMessage(byte[] adpu, int id) {
        super(TYPE, id);
        this.adpu = adpu;
    }

    public CardAdpuResponseMessage(int id) {
        super(TYPE, id);
    }

    @Override
    public void write(DataOutputStream dout) throws IOException {
    	super.write(dout);
        if(adpu != null) {
            dout.writeInt(adpu.length);
            dout.write(adpu);
        } else {
            dout.writeInt(0);
        }
    }
    
    @Override
    public void read(DataInputStream din) throws IOException {
    	super.read(din);
    	
    	int count = din.readInt();
    	adpu = new byte[count];
    	din.readFully(adpu);
    }

    public byte[] getAdpu() {
        return adpu;
    }
}
