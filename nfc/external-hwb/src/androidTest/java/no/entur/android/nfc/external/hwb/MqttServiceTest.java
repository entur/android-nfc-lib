package no.entur.android.nfc.external.hwb;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import no.entur.android.nfc.external.mqtt.test.WebSocketNfcServer;

@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4.class)
public class MqttServiceTest {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MqttServiceTest.class);



    @Test
    public void connect() throws Exception {
        Context applicationContext = ApplicationProvider.getApplicationContext();

        int port = 1883;

        WebSocketNfcServer server = new WebSocketNfcServer(port);
        server.start();
        Thread.sleep(1000);
        System.out.println(server.getAddress());
        try {
            Mqtt3BlockingClient mqtt3BlockingClient = Mqtt3Client.builder()
                    .identifier(UUID.randomUUID().toString())
                    .serverHost("localhost")
                    .serverPort(port)
                    .buildBlocking();

            Mqtt3ConnAck connect = mqtt3BlockingClient.connect();

            System.out.println(connect);

            Thread.sleep(1000);
        } finally {
            server.stop();
        }


    }

}
