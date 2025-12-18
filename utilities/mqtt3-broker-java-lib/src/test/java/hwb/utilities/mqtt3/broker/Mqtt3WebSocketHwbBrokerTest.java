package hwb.utilities.mqtt3.broker;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import no.entur.android.nfc.external.mqtt3.broker.Mqtt3WebSocketBroker;

public class Mqtt3WebSocketHwbBrokerTest {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Mqtt3WebSocketHwbBrokerTest.class);

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

            System.out.println(connect);

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

}
