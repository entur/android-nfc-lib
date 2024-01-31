package no.entur.android.nfc.websocket.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class NfcStatusResponseMessage extends NfcMessage {

	private int status;

	public void setStatus(int status) {
		this.status = status;
	}
	
	public int getStatus() {
		return status;
	}
	
    public void write(DataOutputStream dout) throws IOException {
    	super.write(dout);
        dout.writeInt(status);
    }

    public void read(DataInputStream din) throws IOException {
    	super.read(din);
    	status = din.readInt();
    }
}
