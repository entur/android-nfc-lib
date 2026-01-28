package no.entur.abt.nfc.example;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import no.entur.android.nfc.external.hid.Atr210MqttHandler;
import no.entur.android.nfc.external.hid.HidMqttService;
import no.entur.android.nfc.external.hid.dto.atr210.heartbeat.HeartbeatResponse;
import no.entur.android.nfc.external.hid.test.Atr210MessageSequence;
import no.entur.android.nfc.external.hid.test.HidServiceConnection;
import no.entur.android.nfc.external.hid.test.HidServiceConnector;
import no.entur.android.nfc.external.hid.test.configuration.Atr210ConfigurationEmulator;
import no.entur.android.nfc.external.hid.test.configuration.DefaultAtr210ConfigurationListener;
import no.entur.android.nfc.external.hid.test.heartbeat.Atr210HeartbeatEmulator;
import no.entur.android.nfc.external.hid.test.tag.Atr210MqttTag;
import no.entur.android.nfc.external.hid.test.tag.Atr210TagEmulator;
import no.entur.android.nfc.external.mqtt.test.MqttBrokerServiceConnection;
import no.entur.android.nfc.external.mqtt.test.MqttBrokerServiceConnector;
import no.entur.android.nfc.external.mqtt3.client.MqttServiceClient;
import no.entur.android.nfc.wrapper.test.tech.transceive.ListMockTransceive;

@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4.class)
public class HidMqttReaderTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HidMqttReaderTest.class);

    public ActivityScenarioRule rule = new ActivityScenarioRule(MainActivity.class);

    @Rule
    public RuleChain chain;

    public HidMqttReaderTest() {
        chain = RuleChain.outerRule(rule);
    }

    @Test
    public void connectReader1() throws Exception {
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

    private final static Atr210MessageSequence sequence = new Atr210MessageSequence();

    protected final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void connect() throws Exception {

        Context applicationContext = ApplicationProvider.getApplicationContext();

        MqttBrokerServiceConnector brokerConnector = MqttBrokerServiceConnector.newBuilder().withContext(applicationContext).build();

        MqttBrokerServiceConnection brokerConnection = brokerConnector.connect(true);
        try {
            brokerConnection.start();

            HidServiceConnector serviceConnector = HidServiceConnector.newBuilder()
                    .withContext(applicationContext)
                    .withHost(brokerConnection.getHost())
                    .withPort(brokerConnection.getPort())
                    .withDefaultWebsocketConfiguration()
                    .withLogApdus(true)
                    .build();

            HidServiceConnection serviceConnection = serviceConnector.connect(true);
            try {
                HidMqttService service = serviceConnection.getService();
                Atr210MqttHandler handler = service.getHandler();

                HeartbeatResponse heartbeatResponse = objectMapper.readValue(getClass().getResourceAsStream("/atr210/heartbeat.json"), HeartbeatResponse.class);
                Atr210HeartbeatEmulator heartbeat = new Atr210HeartbeatEmulator(heartbeatResponse, brokerConnection, sequence);

                DefaultAtr210ConfigurationListener listener = new DefaultAtr210ConfigurationListener();
                listener.setEnabled(true);
                listener.setHfReader("testHfName", "testHfId");
                listener.setEnabledHfReader(false); // assume automatically enabled
                handler.enableNfcAutoNfcConfiguration(true, false);

                Atr210ConfigurationEmulator configurationEmulator = new Atr210ConfigurationEmulator(heartbeatResponse.getDeviceType(), heartbeatResponse.getDeviceId(), "123", brokerConnection, sequence, listener);

                try {
                    heartbeat.start();

                    Thread.sleep(500);

                    Set<String> readerIds = handler.getReaderIds();
                    assertEquals(readerIds.size(), 1);

                    // check that readers have enabled HF
                    assertTrue(listener.isEnabled());
                    assertTrue(listener.isEnabledHfReader());

                    System.out.println("Got reader " + readerIds.iterator().next());

                    Atr210TagEmulator readerEmulator = new Atr210TagEmulator(heartbeatResponse.getDeviceType(), heartbeatResponse.getDeviceId(), "123", brokerConnection, sequence);

                    Atr210MqttTag tag = Atr210MqttTag.newBuilder().withDesfireEV1(b -> {
                        b.withTransceive(ListMockTransceive.newBuilder()
                                .withErrorResponse("63") // raw desfire response
                                .withTransceiveNativeDesfireEV1SelectApplication("008057", "00") // raw desfire command
                                .build());
                    }).build();

                    readerEmulator.tagPresent(tag);

                    Thread.sleep(500);

                    onView(withId(R.id.tagStatus)).check(matches(withText("Present")));

                    readerEmulator.tagLost();

                    Thread.sleep(500);
                } finally {
                    configurationEmulator.close();
                }
            } finally {
                serviceConnection.close();
            }
        } finally {
            brokerConnection.close();
        }

    }

}
