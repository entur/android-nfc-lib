package no.entur.android.nfc.websocket.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TagAdpuResponseMessage extends NfcMessage {

    public static final int TYPE = 11;
    
    private byte[] adpu;
    
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
