package no.entur.android.nfc.websocket.messages.card;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import no.entur.android.nfc.websocket.messages.NfcMessage;

public class CardApduRequestMessage extends NfcMessage {

    public static final int TYPE = 103;
    
    private byte[] apdu;

	public CardApduRequestMessage(byte[] apdu) {
		this(nextId());
        this.apdu = apdu;
	}

	public CardApduRequestMessage(int id) {
		super(TYPE, id);
	}

	public CardApduRequestMessage() {
		this(nextId());
    }


	public void setApdu(byte[] apdu) {
		this.apdu = apdu;
	}
    
    public byte[] getApdu() {
		return apdu;
	}
    
    @Override
    public void write(DataOutputStream dout) throws IOException {
    	super.write(dout);
    	dout.writeInt(apdu.length);
    	dout.write(apdu);
    }
    
    @Override
    public void read(DataInputStream din) throws IOException {
    	super.read(din);
    	
    	int count = din.readInt();
    	apdu = new byte[count];
    	din.readFully(apdu);
    }

}
