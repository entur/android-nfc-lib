package no.entur.android.nfc.mqtt.messages.card;

import java.io.IOException;
import java.util.List;

import no.entur.android.nfc.mqtt.messages.RequestResponseMessage;
import no.entur.android.nfc.mqtt.messages.RequestResponseMessageListener;
import no.entur.android.nfc.mqtt.messages.RequestResponseMessages;

public abstract class CardClient<T> implements RequestResponseMessageListener<T> {

	public interface Listener {

		void onCardLost();

		void onCardPresent(CardClient client, List<String> technologies, byte[] atr, byte[] historicalBytes, byte[] uid);
	}

	private final RequestResponseMessages messages;

	private long timeout;

	private Listener listener;

	public CardClient(RequestResponseMessages messages, long timeout) {
		this.messages = messages;
		this.timeout = timeout;
	}

	public void setListener(Listener listener) {
		this.listener = listener;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public byte[] transcieve(byte[] message) throws IOException  {
		RequestResponseMessage response = messages.sendAndWaitForResponse(new CardAdpuRequestMessage(generateId(), message), timeout);
		
		if(response != null) {
			CardAdpuResponseMessage result = (CardAdpuResponseMessage)response;
			return result.getAdpu();
		}
		throw new IOException();
	}

    protected abstract T generateId();

}
