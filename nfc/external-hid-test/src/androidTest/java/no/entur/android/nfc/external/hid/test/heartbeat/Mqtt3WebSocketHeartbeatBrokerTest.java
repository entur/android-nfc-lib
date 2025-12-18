package no.entur.android.nfc.external.hid.test.heartbeat;

import static org.junit.Assert.assertEquals;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.Set;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.android.LogcatAppender;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import no.entur.android.nfc.external.hid.Atr210MqttHandler;
import no.entur.android.nfc.external.hid.HidMqttService;
import no.entur.android.nfc.external.hid.dto.atr210.heartbeat.HeartbeatResponse;
import no.entur.android.nfc.external.hid.test.Atr210MessageSequence;
import no.entur.android.nfc.external.hid.test.HidServiceConnection;
import no.entur.android.nfc.external.hid.test.HidServiceConnector;
import no.entur.android.nfc.external.mqtt.test.MqttBrokerServiceConnection;
import no.entur.android.nfc.external.mqtt.test.MqttBrokerServiceConnector;

public class Mqtt3WebSocketHeartbeatBrokerTest {

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


    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Mqtt3WebSocketHeartbeatBrokerTest.class);

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
                Atr210Heartbeat heartbeat = new Atr210Heartbeat(heartbeatResponse, brokerConnection, sequence);

                heartbeat.start();

                Thread.sleep(2000);

                Set<String> readerIds = handler.getReaderIds();
                assertEquals(readerIds.size(), 1);

                System.out.println("Got " + readerIds);

            } finally {
                serviceConnection.close();
            }

        } finally {
            brokerConnection.close();
        }

    }

}
