package no.entur.android.nfc.mqtt.messages.card;

import java.io.IOException;

import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestResponseMessages;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedResponseMessage;

public class CardCommands<T, C extends CardContext> {

	protected final SynchronizedRequestResponseMessages<T> exchange;

	protected long adpuTranscieveTimeout;

	protected final CardAdpuMessageConverter<T, C> cardAdpuMessageConverter;

	protected final C cardContext;

	public CardCommands(C cardContext, SynchronizedRequestResponseMessages<T> exchange, long adpuTranscieveTimeout, CardAdpuMessageConverter<T, C> cardAdpuMessageConverter) {
		this.cardContext = cardContext;
        this.exchange = exchange;
        this.adpuTranscieveTimeout = adpuTranscieveTimeout;
		this.cardAdpuMessageConverter = cardAdpuMessageConverter;
    }

	public void setAdpuTranscieveTimeout(long adpuTranscieveTimeout) {
		this.adpuTranscieveTimeout = adpuTranscieveTimeout;
	}

	public byte[] transceive(byte[] message) throws IOException  {
		CardAdpuSynchronizedRequestMessageRequest<T, ?> request = cardAdpuMessageConverter.createCardAdpuRequestMessage(message, cardContext);
		SynchronizedResponseMessage<T> response = exchange.sendAndWaitForResponse(request, adpuTranscieveTimeout);
		
		if(response != null) {
			CardAdpuSynchronizedResponseMessage result = cardAdpuMessageConverter.createCardAdpuResponseMessage(response, cardContext);
			return result.getAdpu();
		}
		throw new IOException();
	}


}
