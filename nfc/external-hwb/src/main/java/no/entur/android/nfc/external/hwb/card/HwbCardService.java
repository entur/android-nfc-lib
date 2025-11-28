package no.entur.android.nfc.external.hwb.card;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import hwb.utilities.validators.nfc.CardContent;
import hwb.utilities.validators.nfc.apdu.receive.ReceiveSchema;
import no.entur.android.nfc.external.hwb.HwbMqttClient;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestMessageListener;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestMessageRequest;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestResponseMessages;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedResponseMessageListener;

public class HwbCardService implements SynchronizedRequestMessageListener<UUID> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HwbCardService.class);

    private static final HwbCardMessageConverter CONVERTER = new HwbCardMessageConverter();

    private final SynchronizedRequestResponseMessages<UUID> adpuRequestResponseMessages;

    private HwbCardContext cardContext;

    private HwbCardCommands commands;

    private long transcieveTimeout;


    private HwbMqttClient hwbMqttClient;

    public HwbCardService(SynchronizedRequestResponseMessages<UUID> adpuRequestResponseMessages, HwbMqttClient hwbMqttClient, long transcieveTimeout) {
        this.adpuRequestResponseMessages = adpuRequestResponseMessages;
        this.hwbMqttClient = hwbMqttClient;
        this.transcieveTimeout = transcieveTimeout;
    }

    public void setCardContext(HwbCardContext cardContext) {
        this.cardContext = cardContext;

        this.commands = HwbCardCommands.newBuilder()
                .withCardContext(cardContext)
                .withCardMessageConverter(CONVERTER)
                .withAdpuExchange(adpuRequestResponseMessages)
                .withAdpuTranscieveTimeout(transcieveTimeout)
                .build();
    }

    public void onAdpuResponse(ReceiveSchema receiveSchema) {
        adpuRequestResponseMessages.onResponseMessage(new HwbCardAdpuSynchronizedResponseMessage(receiveSchema));
    }

    @Override
    public void onRequestMessage(SynchronizedRequestMessageRequest<UUID> message, SynchronizedResponseMessageListener<UUID> listener) throws IOException {
        hwbMqttClient.publish(message.getTopic(), message.getPayload());
    }

    public void createTag(String travelCardNumber, String token, List<CardContent> cardContent) {

    }

    public void clearCardContext() {
        HwbCardContext cardContext = this.cardContext;
        if(cardContext != null) {
            cardContext.setClosed(true);

            this.cardContext = null;
        }
    }

    public void close() {
        clearCardContext();
    }

}
