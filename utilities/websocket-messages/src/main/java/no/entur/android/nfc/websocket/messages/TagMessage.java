package no.entur.android.nfc.websocket.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TagMessage extends NfcMessage {

    public static final int TYPE = 10;
    
    private List<String> technologies;
    
    @Override
    public void write(DataOutputStream dout) throws IOException {
    	super.write(dout);
    	dout.writeInt(technologies.size());
    	for (String tech : technologies) {
			dout.writeUTF(tech);
		}
    }
    
    @Override
    public void read(DataInputStream din) throws IOException {
    	super.read(din);
    	
    	int count = din.readInt();
    	technologies = new ArrayList<>(count);
    	for(int i = 0; i < count; i++) {
    		technologies.add(din.readUTF());
    	}
    }

}
