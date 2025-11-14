package no.entur.android.nfc.mqtt.messages.reader;

import java.io.IOException;

import no.entur.android.nfc.mqtt.messages.RequestMessageListener;
import no.entur.android.nfc.mqtt.messages.RequestResponseMessages;
import no.entur.android.nfc.mqtt.messages.ResponseMessage;
import no.entur.android.nfc.mqtt.messages.ResponseMessageListener;

public abstract class Reader<T, D> implements RequestMessageListener<T>, ResponseMessageListener<T> {

	protected final D deviceId;

	protected final RequestResponseMessages adpuExchange;

	protected final RequestResponseMessages readerExchange;

	protected long transcieveTimeout;
	protected long presentTimeout;

	public Reader(D deviceId, RequestResponseMessages adpuExchange, RequestResponseMessages readerExchange, long transcieveTimeout, long presentTimeout) {
        this.deviceId = deviceId;
        this.adpuExchange = adpuExchange;
        this.readerExchange = readerExchange;
        this.transcieveTimeout = transcieveTimeout;
		this.presentTimeout = presentTimeout;
	}

	public void setTranscieveTimeout(long transcieveTimeout) {
		this.transcieveTimeout = transcieveTimeout;
	}

	public byte[] transcieve(byte[] message) throws IOException  {
		CardAdpuRequestMessage<T> request = createCardAdpuRequestMessage(message);
		ResponseMessage<T> response = adpuExchange.sendAndWaitForResponse(request, transcieveTimeout);
		
		if(response != null) {
			CardAdpuResponseMessage result = createCardAdpuResponseMessage(response);
			return result.getAdpu();
		}
		throw new IOException();
	}

    protected abstract CardAdpuRequestMessage<T> createCardAdpuRequestMessage(byte[] message);

	protected abstract CardAdpuResponseMessage createCardAdpuResponseMessage(ResponseMessage<T> message);

	public D getDeviceId() {
		return deviceId;
	}


	public boolean isPresent() throws IOException  {
		ReaderPresentRequestMessage<T> request = createReaderPresentRequestMessage();
		ResponseMessage<T> response = adpuExchange.sendAndWaitForResponse(request, presentTimeout);

		if(response != null) {
			ReaderPresentResponseMessage result = createReaderPresentResponseMessage(response);
			return result.isPresent();
		}
		throw new IOException();
	}

	protected abstract ReaderPresentRequestMessage<T> createReaderPresentRequestMessage();

	protected abstract ReaderPresentResponseMessage createReaderPresentResponseMessage(ResponseMessage<T> message);

}
