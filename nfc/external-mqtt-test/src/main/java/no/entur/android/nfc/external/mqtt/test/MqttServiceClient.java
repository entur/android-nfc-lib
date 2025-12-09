package no.entur.android.nfc.external.mqtt.test;

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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

public class MqttServiceClient {
    protected ObjectMapper objectMapper = new ObjectMapper();

    protected final Executor executor;
    protected final Mqtt3AsyncClient client;

    protected final long connectionTimeout;

    public MqttServiceClient(Executor executor, Mqtt3AsyncClient client, long connectionTimeout) {
        this.executor = executor;
        this.client = client;
        this.connectionTimeout = connectionTimeout;
    }

    public void publish(String topic, Object body) throws IOException {
        byte[] payload = objectMapper.writeValueAsBytes(body);

        publish(topic, payload);
    }

    public void publish(String topic, byte[] payload) {
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

    public <T, R> void exchange(MqttServiceExchange<T, R> exchange) {
        subscribe(exchange.getReadTopic(), (c) -> {
            try {
                byte[] apply = exchange.apply(c.getPayloadAsBytes());

                publish(exchange.getWriteTopic(), apply);
            } catch(Exception e) {
                exchange.getErrorHandler().accept(e);
            }
        });
    }

    public <T> T unmarshal(Mqtt3Publish publish, Class<T> cls) throws IOException {
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
