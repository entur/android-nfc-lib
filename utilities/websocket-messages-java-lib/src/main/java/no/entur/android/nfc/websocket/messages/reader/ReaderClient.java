package no.entur.android.nfc.websocket.messages.reader;

import no.entur.android.nfc.websocket.messages.NfcMessage;
import no.entur.android.nfc.websocket.messages.NfcMessageListener;
import no.entur.android.nfc.websocket.messages.RequestResponseMessages;
import no.entur.android.nfc.websocket.messages.reader.broadcast.ReaderDisconnectedMessage;

public class ReaderClient implements NfcMessageListener {

	public interface Listener {

		void onReaderDisconnected();

	}

	private final RequestResponseMessages messages;
	
	private long timeout;

	private Listener listener;
	
	public ReaderClient(RequestResponseMessages messages, long timeout) {
		this.messages = messages;
		this.timeout = timeout;
	}

	public void setListener(Listener listener) {
		this.listener = listener;
	}

	public void setTimeout(long timeout) {
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

	public boolean disconnect() {
		NfcMessage response = messages.sendAndWaitForResponse(new ReaderDisconnectRequestMessage(), timeout);

		if(response != null) {
			ReaderDisconnectResponseMessage message = (ReaderDisconnectResponseMessage)response;
			return message.getStatus() == 0;
		}
		return false;
	}

	public boolean beginPolling() {
		NfcMessage response = messages.sendAndWaitForResponse(new ReaderBeginPollingRequestMessage(), timeout);

		if(response != null) {
			ReaderBeginPollingResponseMessage message = (ReaderBeginPollingResponseMessage)response;
			return message.getStatus() == 0;
		}
		return false;
	}

	public boolean endPolling() {
		NfcMessage response = messages.sendAndWaitForResponse(new ReaderEndPollingRequestMessage(), timeout);

		if(response != null) {
			ReaderEndPollingResponseMessage message = (ReaderEndPollingResponseMessage)response;
			return message.getStatus() == 0;
		}
		return false;
	}

	@Override
	public void onMessage(NfcMessage message) {
		if(message instanceof ReaderDisconnectedMessage) {
			listener.onReaderDisconnected();
		}
	}

}
