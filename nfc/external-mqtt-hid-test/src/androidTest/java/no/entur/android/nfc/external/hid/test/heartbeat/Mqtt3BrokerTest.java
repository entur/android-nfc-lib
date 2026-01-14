package no.entur.android.nfc.external.hid.test.heartbeat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.android.LogcatAppender;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import no.entur.android.nfc.external.ExternalNfcTagCallback;
import no.entur.android.nfc.external.ExternalNfcTagCallbackSupport;
import no.entur.android.nfc.external.hid.Atr210MqttHandler;
import no.entur.android.nfc.external.hid.HidMqttService;
import no.entur.android.nfc.external.hid.dto.atr210.heartbeat.HeartbeatResponse;
import no.entur.android.nfc.external.hid.test.Atr210MessageSequence;
import no.entur.android.nfc.external.hid.test.HidServiceConnection;
import no.entur.android.nfc.external.hid.test.HidServiceConnector;
import no.entur.android.nfc.external.hid.test.configuration.Atr210ConfigurationEmulator;
import no.entur.android.nfc.external.hid.test.configuration.DefaultAtr210ConfigurationListener;
import no.entur.android.nfc.external.hid.test.tag.Atr210TagEmulator;
import no.entur.android.nfc.external.hid.test.tag.Atr210MqttTag;
import no.entur.android.nfc.external.mqtt.test.MqttBrokerServiceConnection;
import no.entur.android.nfc.external.mqtt.test.MqttBrokerServiceConnector;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;
import no.entur.android.nfc.wrapper.Tag;
import no.entur.android.nfc.wrapper.tech.IsoDep;
import no.entur.android.nfc.wrapper.test.tech.transceive.ListMockTransceive;

@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4.class)
public class Mqtt3BrokerTest {

    static {
        configureLogbackDirectly();
    }

    private static void configureLogbackDirectly() {
        // reset the default context (which may already have been initialized)
        // since we want to reconfigure it
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        lc.stop();

        // setup LogcatAppender
        PatternLayoutEncoder encoder2 = new PatternLayoutEncoder();
        encoder2.setContext(lc);
        encoder2.setPattern("[%thread] %msg%n");
        encoder2.start();

        LogcatAppender logcatAppender = new LogcatAppender();
        logcatAppender.setContext(lc);
        logcatAppender.setEncoder(encoder2);
        logcatAppender.start();

        // add the newly created appenders to the root logger;
        // qualify Logger to disambiguate from org.slf4j.Logger
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.addAppender(logcatAppender);
    }


    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Mqtt3BrokerTest.class);

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


                ExternalNfcTagCallback callback = new ExternalNfcTagCallback() {
                    @Override
                    public void onTagDiscovered(Tag tag, Intent intent) {

                        LOGGER.info("onTagDiscovered");

                        IsoDep isoDep = IsoDep.get(tag);


                        try {
                            try {
                                isoDep.connect();

                                byte[] transceive = isoDep.transceive(new byte[]{0x5A, 0x00, (byte) 0x80, 0x57});
                                LOGGER.info("transceive response " + ByteArrayHexStringConverter.toHexString(transceive));
                            } finally {
                                isoDep.close();
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                    }
                };
                ExternalNfcTagCallbackSupport externalNfcTagCallbackSupport = new ExternalNfcTagCallbackSupport(callback, applicationContext, null, true);
                externalNfcTagCallbackSupport.setEnabled(true);
                externalNfcTagCallbackSupport.onResume();

                readerEmulator.tagPresent(tag);

                Thread.sleep(500);

                readerEmulator.tagLost();

                externalNfcTagCallbackSupport.onPause();

                Thread.sleep(500);

            } finally {
                serviceConnection.close();
            }

        } finally {
            brokerConnection.close();
        }

    }

}
