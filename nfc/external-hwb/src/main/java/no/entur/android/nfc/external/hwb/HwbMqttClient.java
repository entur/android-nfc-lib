package no.entur.android.nfc.external.hwb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAckReturnCode;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestMessageRequest;

public class HwbMqttClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(HwbMqttClient.class);

    protected ObjectMapper objectMapper = new ObjectMapper();

    protected final Executor executor;
    protected final Mqtt3AsyncClient client;

    protected final long connectionTimeout;

    public HwbMqttClient(Executor executor, Mqtt3AsyncClient client, long connectionTimeout) {
        this.executor = executor;
        this.client = client;
        this.connectionTimeout = connectionTimeout;
    }

    public void onRequestMessage(SynchronizedRequestMessageRequest<UUID> message) throws IOException {
        byte[] payload = objectMapper.writeValueAsBytes(message.getPayload());

        client.publishWith()
                .topic(message.getTopic())
                .qos(MqttQos.EXACTLY_ONCE)
                .payload(payload)
                .send();
    }

    public void publish(String topic, Object body) throws IOException {
        byte[] payload = objectMapper.writeValueAsBytes(body);

        client.publishWith()
                .topic(topic)
                .qos(MqttQos.EXACTLY_ONCE)
                .payload(payload)
                .send();
    }

    public void subscribe(String topic, Consumer<Mqtt3Publish> callback) {
        client.subscribeWith()
                .topicFilter(topic)
                .qos(MqttQos.EXACTLY_ONCE)
                .callback(callback)
                .executor(executor)
                .send();
    }

    public <T> void subscribe(String topic, Class<T> clz, Consumer<T> callback) {
        subscribe(topic, (c) -> {
            try {
                T unmarshal = unmarshal(c, clz);
                callback.accept(unmarshal);
            } catch(Exception e) {
                LOGGER.error("Problem handling message for topic {}", topic, e);
            }
        });
    }

    private <T> T unmarshal(Mqtt3Publish publish, Class<T> cls) throws IOException {
        byte[] payloadAsBytes = publish.getPayloadAsBytes();

        return objectMapper.readValue(payloadAsBytes, cls);
    }

    public boolean connect() throws Exception {
        CompletableFuture<@NotNull Mqtt3ConnAck> connect = client.connect();

        Mqtt3ConnAck ack = connect.get(connectionTimeout, TimeUnit.MILLISECONDS);

        return ack.getReturnCode() == Mqtt3ConnAckReturnCode.SUCCESS;
    }

    public void disconnect() {
        client.disconnect();
    }

    public void unsubscribe(String topic) {
        client.unsubscribeWith()
                .topicFilter(topic)
                .send();
    }
}
