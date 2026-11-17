package no.entur.android.nfc.websocket.messages.reader;

import java.util.List;

import javax.smartcardio.CardException;

import no.entur.android.nfc.websocket.messages.NfcMessage;
import no.entur.android.nfc.websocket.messages.NfcMessageListener;
import no.entur.android.nfc.websocket.messages.NfcStatusResponseMessage;
import no.entur.android.nfc.websocket.messages.RequestResponseMessages;
import no.entur.android.nfc.websocket.messages.card.CardServer;
import no.entur.android.nfc.websocket.messages.reader.broadcast.ReaderDisconnectedMessage;

public class ReaderServer implements NfcMessageListener {

	public interface Listener {

		boolean onConnect(List<String> tags);

		boolean onDisconnect();

		boolean onBeginPolling() throws CardException;
		boolean onEndPolling() throws CardException;

	}

	private final NfcMessageListener sender;

	private ReaderServer.Listener listener;

	public ReaderServer(NfcMessageListener nfcMessageListener) {
		this.sender = nfcMessageListener;
	}

	public void setListener(ReaderServer.Listener listener) {
		this.listener = listener;
	}

	@Override
	public void onMessage(NfcMessage message) {
		if(message instanceof ReaderDisconnectRequestMessage) {
			listener.onDisconnect();
			sender.onMessage(new ReaderDisconnectResponseMessage(message.getId()));
		} else if(message instanceof ReaderConnectRequestMessage) {
			ReaderConnectRequestMessage m = (ReaderConnectRequestMessage)message;
			ReaderConnectResponseMessage response = new ReaderConnectResponseMessage(message.getId());
			if(!listener.onConnect(m.getTags())) {
				response.setStatus(NfcStatusResponseMessage.STATUS_READER_UNABLE_TO_CONNECT);
			}
			sender.onMessage(response);
		} else if(message instanceof ReaderBeginPollingRequestMessage) {
			try {
				if(listener.onBeginPolling()) {
					sender.onMessage(new ReaderBeginPollingResponseMessage(message.getId()));
				} else {
					sender.onMessage(new ReaderBeginPollingResponseMessage(message.getId(), NfcStatusResponseMessage.STATUS_READER_UNABLE_TO_START_POLLING));
				}
			} catch (CardException e) {
				sender.onMessage(new ReaderBeginPollingResponseMessage(message.getId(), -1));
			}
		} else if(message instanceof ReaderEndPollingRequestMessage) {
			try {
				listener.onEndPolling();
				
				sender.onMessage(new ReaderEndPollingResponseMessage(message.getId()));
			} catch (CardException e) {
				sender.onMessage(new ReaderEndPollingResponseMessage(message.getId(), -1));

			}
		}
	}

}
