package no.entur.android.nfc.external.hid.card;

import android.os.Parcelable;

import java.io.IOException;

import no.entur.android.nfc.external.hid.dto.atr210.NfcAdpuTransmitRequest;
import no.entur.android.nfc.mqtt.messages.card.CardCommands;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestResponseMessages;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedResponseMessage;
import no.entur.android.nfc.wrapper.tech.utils.bulk.BulkTransceiveCommands;

public class Atr210CardCommands extends CardCommands<String, Atr210CardContext> {

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private Atr210CardContext cardContext;
        private SynchronizedRequestResponseMessages<String> requestResponseMessages;
        private long adpuTranscieveTimeout;
        private Atr210CardMessageConverter cardMessageConverter;

        public Builder withCardContext(Atr210CardContext cardContext) {
            this.cardContext = cardContext;
            return this;
        }

        public Builder withAdpuExchange(SynchronizedRequestResponseMessages<String> requestResponseMessages) {
            this.requestResponseMessages = requestResponseMessages;
            return this;
        }

        public Builder withAdpuTranscieveTimeout(long adpuTranscieveTimeout) {
            this.adpuTranscieveTimeout = adpuTranscieveTimeout;
            return this;
        }

        public Builder withCardMessageConverter(Atr210CardMessageConverter cardMessageConverter) {
            this.cardMessageConverter = cardMessageConverter;
            return this;
        }

        public Atr210CardCommands build() {
            return new Atr210CardCommands(cardContext, requestResponseMessages, adpuTranscieveTimeout, cardMessageConverter);
        }

    }

    public Atr210CardCommands(Atr210CardContext cardContext, SynchronizedRequestResponseMessages<String> adpuExchange, long adpuTranscieveTimeout, Atr210CardMessageConverter cardAdpuMessageConverter) {
        super(cardContext, adpuExchange, adpuTranscieveTimeout, cardAdpuMessageConverter);
    }

    public Parcelable transcieve(Parcelable parcelable) throws IOException {
        if(parcelable instanceof NfcAdpuTransmitRequest) {
            String requestTopic = "itxpt/ticketreader/" + cardContext.getProviderId() + "/nfc/hf/apdu/transmit";
            String responseTopic = "itxpt/ticketreader/" + cardContext.getProviderId() + "/nfc/hf/apdu/response";

            Atr210CardAdpuSynchronizedRequestMessage request = new Atr210CardAdpuSynchronizedRequestMessage((NfcAdpuTransmitRequest) parcelable, requestTopic, responseTopic);
            SynchronizedResponseMessage<String> response = exchange.sendAndWaitForResponse(request, adpuTranscieveTimeout);

            if(response != null) {
                Atr210CardAdpuSynchronizedResponseMessage result = (Atr210CardAdpuSynchronizedResponseMessage) cardAdpuMessageConverter.createCardAdpuResponseMessage(response, cardContext);
                return result.getPayload();
            }
            throw new IOException();
        } else if(parcelable instanceof BulkTransceiveCommands) {

        }

        throw new RuntimeException("Unsupported parcelable type " + parcelable.getClass().getName());
    }

}
