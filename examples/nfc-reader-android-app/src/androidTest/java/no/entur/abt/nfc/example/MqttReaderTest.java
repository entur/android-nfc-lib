package no.entur.abt.nfc.example;

import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import no.entur.android.nfc.external.hid.reader.Atr210ReaderConfiguration;
import no.entur.android.nfc.external.mqtt3.client.MqttServiceClient;

@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4.class)
public class MqttReaderTest {

    @Test
    public void connect() throws Exception {
        Mqtt3AsyncClient mqtt3AsyncClient = Mqtt3Client.builder()
                .identifier(UUID.randomUUID().toString())
                //.webSocketWithDefaultConfig()
                .serverPort(1883)
                .serverHost("192.168.3.30")
                .buildAsync();

        Executor executor = Executors.newSingleThreadExecutor();
        MqttServiceClient client = new MqttServiceClient(executor, mqtt3AsyncClient, 1000);

        if(client.connect()) {
            System.out.println("CONNECTED");
        } else {
            throw new Exception();
        }

        client.subscribe("#", (a) -> {
            byte[] payloadAsBytes = a.getPayloadAsBytes();
            System.out.println(" <- " + a.getTopic().toString() + " " + new String(payloadAsBytes, StandardCharsets.UTF_8));
        });

        Thread.sleep(10000);

    }
}
