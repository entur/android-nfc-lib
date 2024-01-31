package no.entur.android.nfc.websocket.messages;

public class ReaderApi {

	private final RequestResponseMessages messages;
	
	private long timeout;
	
	public ReaderApi(RequestResponseMessages messages, long timeout) {
		this.messages = messages;
		this.timeout = timeout;
	}
	
	
	public boolean connect() {
		NfcMessage response = messages.sendAndWaitForResponse(new ReaderConnectRequestMessage(), timeout);
		
		if(response != null) {
			ReaderConnectResponseMessage message = (ReaderConnectResponseMessage)response;
			return message.getStatus() == 0;
		}
		return false;
	}
}
