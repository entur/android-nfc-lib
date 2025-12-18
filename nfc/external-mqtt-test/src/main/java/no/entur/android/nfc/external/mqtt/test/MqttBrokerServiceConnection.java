package no.entur.android.nfc.external.mqtt.test;

import java.io.Closeable;
import java.io.IOException;

import no.entur.android.nfc.external.mqtt3.broker.Mqtt3TopicListener;

public interface MqttBrokerServiceConnection extends Closeable {

    void publish(String topic, int qos, byte[] payload) throws IOException;

    void start();

    void stop() throws InterruptedException;

    void addListener(Mqtt3TopicListener listener);

    void removeListener(Mqtt3TopicListener listener);

    String getHost();

    int getPort();
}
