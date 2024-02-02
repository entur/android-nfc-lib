package no.entur.android.nfc.websocket.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class NfcStatusResponseMessage extends NfcMessage {

	public static final int STATUS_OK = 0;
	public static final int STATUS_READER_UNABLE_TO_CONNECT = 1000;
	public static final int STATUS_READER_UNABLE_TO_START_POLLING = 1001;

	public static final int STATUS_CARD_UNABLE_TO_TRANSCIEVE = 2000;

	private int status = STATUS_OK;

	public NfcStatusResponseMessage(int type, int id) {
		super(type, id);
	}

	public NfcStatusResponseMessage(int type) {
		super(type);
	}

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
