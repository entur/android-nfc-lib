package no.entur.android.nfc.external.hwb.card;

import android.os.Parcelable;

import java.io.IOException;
import java.util.UUID;

import hwb.utilities.validators.nfc.NfcSchema;
import hwb.utilities.validators.nfc.apdu.deviceId.transmit.TransmitSchema;
import no.entur.android.nfc.mqtt.messages.card.CardCommands;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestResponseMessages;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedResponseMessage;

public class Atr210CardCommands extends CardCommands<UUID, Atr210CardContext> {

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private Atr210CardContext cardContext;
        private SynchronizedRequestResponseMessages<UUID> adpuRequestResponseMessages;
        private long adpuTranscieveTimeout;
        private Atr210CardMessageConverter cardMessageConverter;

        public Builder withCardContext(Atr210CardContext cardContext) {
            this.cardContext = cardContext;
            return this;
        }

        public Builder withAdpuExchange(SynchronizedRequestResponseMessages<UUID> adpuRequestResponseMessages) {
            this.adpuRequestResponseMessages = adpuRequestResponseMessages;
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
            return new Atr210CardCommands(cardContext, adpuRequestResponseMessages, adpuTranscieveTimeout, cardMessageConverter);
        }

    }

    public Atr210CardCommands(Atr210CardContext cardContext, SynchronizedRequestResponseMessages<UUID> adpuExchange, long adpuTranscieveTimeout, Atr210CardMessageConverter cardAdpuMessageConverter) {
        super(cardContext, adpuExchange, adpuTranscieveTimeout, cardAdpuMessageConverter);
    }

    public Parcelable transcieve(Parcelable parcelable) throws IOException {
        if(parcelable instanceof TransmitSchema) {
            Atr210CardAdpuSynchronizedRequestMessage request = new Atr210CardAdpuSynchronizedRequestMessage((TransmitSchema) parcelable);
            SynchronizedResponseMessage<UUID> response = adpuExchange.sendAndWaitForResponse(request, adpuTranscieveTimeout);

            if(response != null) {
                Atr210CardAdpuSynchronizedResponseMessage result = (Atr210CardAdpuSynchronizedResponseMessage) cardAdpuMessageConverter.createCardAdpuResponseMessage(response, cardContext);
                return result.getPayload();
            }
            throw new IOException();
        }

        throw new RuntimeException("Unsupported parcelable type " + parcelable.getClass().getName());
    }

}
