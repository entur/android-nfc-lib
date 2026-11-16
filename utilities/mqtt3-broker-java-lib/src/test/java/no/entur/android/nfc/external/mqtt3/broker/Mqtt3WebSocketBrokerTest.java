package no.entur.android.nfc.external.mqtt3.broker;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Mqtt3WebSocketBrokerTest {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Mqtt3WebSocketBrokerTest.class);

    @Test
    public void connect() throws Exception {
        Mqtt3WebSocketBroker server = Mqtt3WebSocketBroker.newBuilder().build();

        server.start();
        Thread.sleep(1000);
        System.out.println(server.getAddress());
        try {
            Mqtt3AsyncClient mqtt3AsyncClient = Mqtt3Client.builder()
                    .identifier(UUID.randomUUID().toString())
                    .webSocketWithDefaultConfig()
                    .serverAddress(server.getAddress())
                    .buildAsync();

            Mqtt3ConnAck connect = mqtt3AsyncClient.connect().get();

            AtomicBoolean result = new AtomicBoolean(false);
            mqtt3AsyncClient.subscribeWith().topicFilter("/myTopic").callback((a) -> {
                byte[] payloadAsBytes = a.getPayloadAsBytes();
                System.out.println("Got message " + new String(payloadAsBytes, StandardCharsets.UTF_8));

                result.set(true);
            }).send().get();

            System.out.println("Subscribe finished");

            Thread.sleep(100);

            // https://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718058
            server.publish("/myTopic",  2, "{}".getBytes(StandardCharsets.UTF_8));

            Thread.sleep(100);

            assertTrue(result.get());
        } finally {
            server.stop();
        }

    }

    @Test
    public void publishQos0() throws Exception {
        Mqtt3WebSocketBroker server = Mqtt3WebSocketBroker.newBuilder().build();

        server.start();
        Thread.sleep(1000);
        try {
            Mqtt3AsyncClient client = connect(server);

            CompletableFuture<byte[]> received = new CompletableFuture<>();
            client.subscribeWith().topicFilter("/myTopic").callback((a) -> received.complete(a.getPayloadAsBytes())).send().get();

            Thread.sleep(100);

            server.publish("/myTopic", 0, "{\"a\":1}".getBytes(StandardCharsets.UTF_8));

            assertArrayEquals("{\"a\":1}".getBytes(StandardCharsets.UTF_8), received.get(5, TimeUnit.SECONDS));
        } finally {
            server.stop();
        }
    }

    @Test
    public void forwardPublishBetweenClients() throws Exception {
        forwardPublishBetweenClients(MqttQos.AT_MOST_ONCE);
        forwardPublishBetweenClients(MqttQos.AT_LEAST_ONCE);
        forwardPublishBetweenClients(MqttQos.EXACTLY_ONCE);
    }

    private void forwardPublishBetweenClients(MqttQos qos) throws Exception {
        CompletableFuture<byte[]> brokerReceived = new CompletableFuture<>();

        Mqtt3WebSocketBroker server = Mqtt3WebSocketBroker.newBuilder()
                .withListener((broker, source, topic, payload) -> brokerReceived.complete(payload))
                .build();

        server.start();
        Thread.sleep(1000);
        try {
            Mqtt3AsyncClient subscriber = connect(server);
            Mqtt3AsyncClient publisher = connect(server);

            CompletableFuture<com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish> received = new CompletableFuture<>();
            subscriber.subscribeWith().topicFilter("/myTopic").qos(MqttQos.EXACTLY_ONCE).callback(received::complete).send().get();

            Thread.sleep(100);

            byte[] payload = "{\"b\":2}".getBytes(StandardCharsets.UTF_8);
            publisher.publishWith().topic("/myTopic").qos(qos).payload(payload).send().get(5, TimeUnit.SECONDS);

            assertArrayEquals(payload, brokerReceived.get(5, TimeUnit.SECONDS));

            com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish publish = received.get(5, TimeUnit.SECONDS);
            assertArrayEquals(payload, publish.getPayloadAsBytes());
            assertEquals("/myTopic", publish.getTopic().toString());
            assertEquals(qos, publish.getQos());
        } finally {
            server.stop();
        }
    }

    private static Mqtt3AsyncClient connect(Mqtt3WebSocketBroker server) throws Exception {
        Mqtt3AsyncClient client = Mqtt3Client.builder()
                .identifier(UUID.randomUUID().toString())
                .webSocketWithDefaultConfig()
                .serverAddress(server.getAddress())
                .buildAsync();

        client.connect().get(5, TimeUnit.SECONDS);
        return client;
    }

}
