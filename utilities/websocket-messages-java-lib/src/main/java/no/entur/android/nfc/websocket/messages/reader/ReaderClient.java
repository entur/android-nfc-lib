package no.entur.android.nfc.websocket.messages.reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import no.entur.android.nfc.websocket.messages.NfcMessage;
import no.entur.android.nfc.websocket.messages.NfcMessageListener;
import no.entur.android.nfc.websocket.messages.RequestResponseMessages;
import no.entur.android.nfc.websocket.messages.reader.broadcast.ReaderDisconnectedMessage;

public class ReaderClient implements NfcMessageListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReaderClient.class);

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

	public boolean connect(String[] tags) {
		NfcMessage response = messages.sendAndWaitForResponse(new ReaderConnectRequestMessage(Arrays.asList(tags)), timeout);
		
		if(response != null) {
			ReaderConnectResponseMessage message = (ReaderConnectResponseMessage)response;
			if(message.getStatus() == ReaderConnectResponseMessage.STATUS_OK) {
				return true;
			}
			LOGGER.warn("Connect reader returned " + message.getStatus());
		} else {
			LOGGER.warn("Connect reader response timed out after " + timeout);
		}

		return false;
	}

	public boolean disconnect() {
		NfcMessage response = messages.sendAndWaitForResponse(new ReaderDisconnectRequestMessage(), timeout);

		if(response != null) {
			ReaderDisconnectResponseMessage message = (ReaderDisconnectResponseMessage)response;
			if(message.getStatus() == ReaderConnectResponseMessage.STATUS_OK) {
				return true;
			}
			LOGGER.warn("Disconnect reader returned " + message.getStatus());
		}
		return false;
	}

	public boolean beginPolling() {
		NfcMessage response = messages.sendAndWaitForResponse(new ReaderBeginPollingRequestMessage(), timeout);

		if(response != null) {
			ReaderBeginPollingResponseMessage message = (ReaderBeginPollingResponseMessage)response;
			if(message.getStatus() == ReaderConnectResponseMessage.STATUS_OK) {
				return true;
			}
			LOGGER.warn("Begin polling returned " + message.getStatus());		}
		return false;
	}

	public boolean endPolling() {
		NfcMessage response = messages.sendAndWaitForResponse(new ReaderEndPollingRequestMessage(), timeout);

		if(response != null) {
			ReaderEndPollingResponseMessage message = (ReaderEndPollingResponseMessage)response;
			if(message.getStatus() == ReaderConnectResponseMessage.STATUS_OK) {
				return true;
			}
			LOGGER.warn("End polling returned " + message.getStatus());
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
