package no.entur.android.nfc.websocket.messages.card;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import no.entur.android.nfc.websocket.messages.NfcStatusResponseMessage;

public class CardApduResponseMessage extends NfcStatusResponseMessage {

    public static final int TYPE = 102;
    
    private byte[] apdu;

    public CardApduResponseMessage(byte[] apdu, int id) {
        super(TYPE, id);
        this.apdu = apdu;
    }

    public CardApduResponseMessage(int id) {
        super(TYPE, id);
    }

    @Override
    public void write(DataOutputStream dout) throws IOException {
    	super.write(dout);
        if(apdu != null) {
            dout.writeInt(apdu.length);
            dout.write(apdu);
        } else {
            dout.writeInt(0);
        }
    }
    
    @Override
    public void read(DataInputStream din) throws IOException {
    	super.read(din);
    	
    	int count = din.readInt();
    	apdu = new byte[count];
    	din.readFully(apdu);
    }

    public byte[] getApdu() {
        return apdu;
    }
}
