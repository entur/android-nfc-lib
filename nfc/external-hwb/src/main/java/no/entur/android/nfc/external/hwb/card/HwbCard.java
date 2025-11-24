package no.entur.android.nfc.external.hwb.card;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;

import hwb.utilities.validators.nfc.CardContent;
import hwb.utilities.validators.nfc.apdu.receive.ReceiveSchema;
import no.entur.android.nfc.external.hwb.reader.HwbReader;
import no.entur.android.nfc.mqtt.messages.ObjectMqttMessage;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestMessageListener;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestMessageRequest;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestResponseMessages;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedResponseMessageListener;

public class HwbCard implements SynchronizedRequestMessageListener<UUID> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HwbCard.class);

    private static final HwbCardMessageConverter cardMessageConverter = new HwbCardMessageConverter();

    private ObjectMapper objectMapper;

    private Executor executor;
    private Mqtt3AsyncClient client;
    private SynchronizedRequestResponseMessages<UUID> adpuRequestResponseMessages = new SynchronizedRequestResponseMessages<>();

    private HwbCardContext context;

    private HwbCardCommands commands;

    private long transcieveTimeout = 683;

    public void setContext(HwbCardContext context) {
        this.context = context;

        this.commands = HwbCardCommands.newBuilder()
                .withCardContext(context)
                .withCardMessageConverter(cardMessageConverter)
                .withAdpuExchange(adpuRequestResponseMessages)
                .withAdpuTranscieveTimeout(transcieveTimeout)
                .build();
    }

    public void adpu(Mqtt3Publish publish) {
        try {
            byte[] payloadAsBytes = publish.getPayloadAsBytes();

            ReceiveSchema receiveSchema = objectMapper.readValue(payloadAsBytes, ReceiveSchema.class);

            if(receiveSchema.getDeviceId().equals(context.getDeviceId())) {
                adpuRequestResponseMessages.onResponseMessage(new HwbCardAdpuSynchronizedResponseMessage(receiveSchema));
            }
        } catch (Exception e) {
            LOGGER.error("Problem parsing ADPU MQTT message", e);
        }
    }

    @Override
    public void onRequestMessage(SynchronizedRequestMessageRequest<UUID> message, SynchronizedResponseMessageListener<UUID> listener) throws IOException {
        byte[] payload = objectMapper.writeValueAsBytes(message.getPayload());

        client.publishWith()
                .topic(message.getTopic())
                .qos(MqttQos.EXACTLY_ONCE)
                .payload(payload)
                .send();
    }

    public void subscribe() {
        // subscribes to topics
        // /validators/nfc/apdu/receive <- shared topic, used here only for ADPU exchange
        client.subscribeWith()
                .topicFilter("/validators/nfc/apdu/receive")
                .qos(MqttQos.EXACTLY_ONCE)
                .callback(this::adpu)
                .executor(executor)
                .send();

    }

    public void unsubscribe() {
        client.unsubscribeWith()
                .topicFilter("/device/" + context.getDeviceId() + "/diagnostics")
                .send();

        commands = null;
    }

    public void createTag(String travelCardNumber, String token, List<CardContent> cardContent) {



    }
}
