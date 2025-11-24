package no.entur.android.nfc.external.hwb.card;

import java.util.UUID;

import no.entur.android.nfc.mqtt.messages.card.CardCommands;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestResponseMessages;

public class HwbCardCommands extends CardCommands<UUID, HwbCardContext> {

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private HwbCardContext cardContext;
        private SynchronizedRequestResponseMessages<UUID> adpuRequestResponseMessages;
        private long adpuTranscieveTimeout;
        private HwbCardMessageConverter cardMessageConverter;

        public Builder withCardContext(HwbCardContext cardContext) {
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

        public Builder withCardMessageConverter(HwbCardMessageConverter cardMessageConverter) {
            this.cardMessageConverter = cardMessageConverter;
            return this;
        }

        public HwbCardCommands build() {
            return new HwbCardCommands(cardContext, adpuRequestResponseMessages, adpuTranscieveTimeout, cardMessageConverter);
        }

    }

    public HwbCardCommands(HwbCardContext cardContext, SynchronizedRequestResponseMessages<UUID> adpuExchange, long adpuTranscieveTimeout, HwbCardMessageConverter cardAdpuMessageConverter) {
        super(cardContext, adpuExchange, adpuTranscieveTimeout, cardAdpuMessageConverter);
    }
}
