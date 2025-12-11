package hwb.utilities.mqtt3.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import hwb.utilities.device.diagnostics.request.RequestSchema;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MqttServiceClientTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void connect() throws Exception {
        Mqtt3AsyncClient mqtt3AsyncClient = Mqtt3Client.builder()
                .identifier(UUID.randomUUID().toString())
                //.webSocketWithDefaultConfig()
                .serverPort(1883)
                .serverHost("192.168.3.30")
                .buildAsync();

        RequestSchema ping = new RequestSchema();
        ping.setTraceId(UUID.randomUUID());
        ping.setEventTimestamp(new Date());

        Executor executor = Executors.newSingleThreadExecutor();
        MqttServiceClient client = new MqttServiceClient(executor, mqtt3AsyncClient, 1000);

        client.connect();

        client.subscribe("itxpt/ticketreader/+/nfc/readers/configuration", (a) -> {
            byte[] payloadAsBytes = a.getPayloadAsBytes();
            System.out.println(" <- " + a.getTopic().toString() + " " + new String(payloadAsBytes, StandardCharsets.UTF_8));

            String providerId = a.getTopic().getLevels().get(2);

            try {
                Atr210ReaderConfiguration configuration = objectMapper.readValue(payloadAsBytes, Atr210ReaderConfiguration.class);

                if(!configuration.isEnabled() || Objects.equals(configuration.getHfId(), "none") || Objects.equals(configuration.getSamId(), "none")) {
                    String topic = "itxpt/ticketreader/" + providerId + "/nfc/readers/request";
                    System.out.println(" -> " + topic);
                    client.publish(topic, "{}");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        client.subscribe("itxpt/ticketreader/+/nfc/readers", (a) -> {
            byte[] payloadAsBytes = a.getPayloadAsBytes();

            System.out.println(" <- " + a.getTopic().toString() + " " + new String(payloadAsBytes, StandardCharsets.UTF_8));
        });

        client.subscribe("itxpt/inventory/connections/+/will", (a) -> {
            byte[] payloadAsBytes = a.getPayloadAsBytes();

            System.out.println(" <- " + a.getTopic().toString() + " " + ByteArrayHexStringConverter.toHexString(payloadAsBytes));

            String clientId = a.getTopic().getLevels().get(3);

            System.out.println("Got client id " + clientId);

            if(payloadAsBytes[0] == 0x31) {
                String topic = "itxpt/ticketreader/" + "itxpt.ticketreader.ATR210EH.1852258710" + "/nfc/readers/configuration/request";
                System.out.println("-> " + topic);
                client.publish(topic, "{}");
            }
        });

        String providerId = "FRATR210EH1852258710";

        String key = "itxpt.ticketreader.ATR210EH.1852258710";

        client.publish("itxpt/ticketreader/" + key + "/nfc/readers/configuration/request", "{}");

        //client.publish("device/diagnostics/request", ping);

        System.out.println("Waiting");

        Thread.sleep(1000 * 60 * 10);

        System.out.println("Done");


    }

}
