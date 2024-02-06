package no.entur.android.nfc.websocket.messages.card;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import no.entur.android.nfc.websocket.messages.NfcMessage;

public class CardAdpuRequestMessage extends NfcMessage {

    public static final int TYPE = 103;
    
    private byte[] adpu;

	public CardAdpuRequestMessage(byte[] adpu) {
		this(nextId());
        this.adpu = adpu;
	}

	public CardAdpuRequestMessage(int id) {
		super(TYPE, id);
	}

	public CardAdpuRequestMessage() {
		this(nextId());
    }


	public void setAdpu(byte[] adpu) {
		this.adpu = adpu;
	}
    
    public byte[] getAdpu() {
		return adpu;
	}
    
    @Override
    public void write(DataOutputStream dout) throws IOException {
    	super.write(dout);
    	dout.writeInt(adpu.length);
    	dout.write(adpu);
    }
    
    @Override
    public void read(DataInputStream din) throws IOException {
    	super.read(din);
    	
    	int count = din.readInt();
    	adpu = new byte[count];
    	din.readFully(adpu);
    }

}
