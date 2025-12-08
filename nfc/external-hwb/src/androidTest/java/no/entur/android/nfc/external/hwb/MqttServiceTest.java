package no.entur.android.nfc.external.hwb;

import static org.junit.Assert.fail;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.android.LogcatAppender;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import io.moquette.broker.Server;
import no.entur.android.nfc.external.hwb.test.HwbMqttServiceClient;

@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4.class)
public class MqttServiceTest {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MqttServiceTest.class);

    public static final String PREF_KEY_EXTERNAL_NFC = "externalNfcService";

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


    @Test
    public void connect() throws Exception {
        Context applicationContext = ApplicationProvider.getApplicationContext();

        HwbMqttServiceClient connection = new HwbMqttServiceClient(applicationContext, 1000);

        connection.bind();
        if(!connection.waitForBind()) {
            fail();
        }

        connection.run( (service) -> {
            Server server = service.getServer();

            System.out.println("Got server");

            Mqtt3AsyncClient mqtt3AsyncClient = Mqtt3Client.builder()
                    .identifier("123")
                    .serverHost("127.0.0.1")
                    .serverPort(1883)
                    .buildAsync();

            Executor executor = Executors.newSingleThreadExecutor();
            HwbMqttServiceClient client = new HwbMqttServiceClient(executor, mqtt3AsyncClient, 1000);

            try {
                try {
                    client.connect();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                System.out.println("MQTT connect");

            } finally {
                mqtt3BlockingClient.disconnect();
            }
        });
    }

}
