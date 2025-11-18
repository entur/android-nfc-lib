package no.entur.android.nfc.external.hwb.reader;

import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;

import no.entur.android.nfc.mqtt.messages.RequestMessage;
import no.entur.android.nfc.mqtt.messages.RequestMessageListener;
import no.entur.android.nfc.mqtt.messages.RequestResponseMessages;
import no.entur.android.nfc.mqtt.messages.ResponseMessage;
import no.entur.android.nfc.mqtt.messages.ResponseMessageListener;
import no.entur.android.nfc.mqtt.messages.reader.CardAdpuRequestMessage;
import no.entur.android.nfc.mqtt.messages.reader.CardAdpuResponseMessage;
import no.entur.android.nfc.mqtt.messages.reader.Reader;
import no.entur.android.nfc.mqtt.messages.reader.ReaderPresentRequestMessage;
import no.entur.android.nfc.mqtt.messages.reader.ReaderPresentResponseMessage;

public class HwbNfcReader extends Reader<String, String> implements RequestMessageListener<String> {

    // subscribes to topics
    // /device/[deviceId]/diagnostics <- private topic
    // /validators/nfc/apdu/receive <- shared topic, used here only for ADPU exchange

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private String deviceId;
        private RequestResponseMessages adpuExchange;

        private long transcieveTimeout;
        private long presentTimeout;

        private Mqtt3AsyncClient mqttClient;

        public HwbNfcReader build() {
            return null;
        }
    }


    private Mqtt3AsyncClient client;

    public HwbNfcReader(String deviceId, RequestResponseMessages adpuExchange, RequestResponseMessages readerExchange, long transcieveTimeout, long presentTimeout) {
        super(deviceId, adpuExchange, readerExchange, transcieveTimeout, presentTimeout);
    }

    @Override
    protected CardAdpuRequestMessage<String> createCardAdpuRequestMessage(byte[] message) {
        return null;
    }

    @Override
    protected CardAdpuResponseMessage createCardAdpuResponseMessage(ResponseMessage<String> message) {
        return null;
    }

    @Override
    protected ReaderPresentRequestMessage<String> createReaderPresentRequestMessage() {
        return null;
    }

    @Override
    protected ReaderPresentResponseMessage createReaderPresentResponseMessage(ResponseMessage<String> message) {
        return null;
    }

    @Override
    public void onRequestMessage(RequestMessage<String> message, ResponseMessageListener<String> listener) {

    }

    @Override
    public void onResponseMessage(ResponseMessage<String> message) {

    }
}
