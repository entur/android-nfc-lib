package no.entur.android.nfc.websocket.messages.reader;

import no.entur.android.nfc.websocket.messages.NfcMessage;
import no.entur.android.nfc.websocket.messages.NfcMessageListener;
import no.entur.android.nfc.websocket.messages.RequestResponseMessages;
import no.entur.android.nfc.websocket.messages.card.CardServer;
import no.entur.android.nfc.websocket.messages.reader.broadcast.ReaderDisconnectedMessage;

public class ReaderServer implements NfcMessageListener {

	public interface Listener {

		boolean onConnect();

		boolean onDisconnect();

		boolean onBeginPolling();
		boolean onEndPolling();

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
			sender.onMessage(new ReaderDisconnectResponseMessage());
		} else if(message instanceof ReaderConnectRequestMessage) {
			listener.onConnect();
			sender.onMessage(new ReaderConnectResponseMessage());
		} else if(message instanceof ReaderConnectRequestMessage) {
			listener.onBeginPolling();
			sender.onMessage(new ReaderBeginPollingRequestMessage());
		} else if(message instanceof ReaderEndPollingRequestMessage) {
			listener.onEndPolling();
			sender.onMessage(new ReaderEndPollingResponseMessage());
		}
	}

}
