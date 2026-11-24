package no.entur.android.nfc.external.hwb.card;

import android.os.Parcelable;

import java.io.IOException;
import java.util.UUID;

import hwb.utilities.validators.nfc.apdu.deviceId.transmit.TransmitSchema;
import hwb.utilities.validators.nfc.apdu.receive.ReceiveSchema;
import no.entur.android.nfc.external.hwb.card.bulk.HwbBulkConverter;
import no.entur.android.nfc.mqtt.messages.card.CardCommands;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestResponseMessages;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedResponseMessage;
import no.entur.android.nfc.wrapper.tech.utils.bulk.BulkTransceiveCommands;

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

    public Parcelable transceive(Parcelable parcelable) throws IOException {
        if(parcelable instanceof TransmitSchema) {
            TransmitSchema transmitSchema = (TransmitSchema) parcelable;

            HwbCardAdpuSynchronizedRequestMessage request = new HwbCardAdpuSynchronizedRequestMessage(transmitSchema);
            SynchronizedResponseMessage<UUID> response = exchange.sendAndWaitForResponse(request, adpuTranscieveTimeout * transmitSchema.getCommand().size());

            if(response != null) {
                HwbCardAdpuSynchronizedResponseMessage result = (HwbCardAdpuSynchronizedResponseMessage) cardAdpuMessageConverter.createCardAdpuResponseMessage(response, cardContext);
                return result.getPayload();
            }
            throw new IOException();
        } else if(parcelable instanceof BulkTransceiveCommands) {
            BulkTransceiveCommands bulkTransceiveCommands = (BulkTransceiveCommands)parcelable;

            HwbBulkConverter converter = new HwbBulkConverter();

            converter.setDeviceId(cardContext.getDeviceId());
            converter.setTraceId(cardContext.getTraceId());

            TransmitSchema transmitSchema = converter.convert(bulkTransceiveCommands);

            HwbCardAdpuSynchronizedRequestMessage request = new HwbCardAdpuSynchronizedRequestMessage(transmitSchema);
            SynchronizedResponseMessage<UUID> response = exchange.sendAndWaitForResponse(request, adpuTranscieveTimeout * transmitSchema.getCommand().size());

            if(response != null) {
                HwbCardAdpuSynchronizedResponseMessage result = (HwbCardAdpuSynchronizedResponseMessage) cardAdpuMessageConverter.createCardAdpuResponseMessage(response, cardContext);
                ReceiveSchema payload = result.getPayload();

                return converter.convert(payload);
            }
            throw new IOException();
        }

        throw new RuntimeException("Unsupported parcelable type " + parcelable.getClass().getName());
    }

}
