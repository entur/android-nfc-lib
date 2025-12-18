package no.entur.android.nfc.external.mqtt.test;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import hwb.utilities.mqtt3.broker.Mqtt3TopicListener;

public class DefaultMqttBrokerServiceConnection implements MqttBrokerServiceConnection {

    private final MqttBrokerServiceConnector connector;
    private final MqttBrokerService delegate;

    private List<Mqtt3TopicListener> listeners = new ArrayList<>();

    private final boolean stopServiceOnClose;

    public DefaultMqttBrokerServiceConnection(MqttBrokerServiceConnector connector, MqttBrokerService delegate, boolean stopServiceOnClose) {
        this.connector = connector;
        this.delegate = delegate;
        this.stopServiceOnClose = stopServiceOnClose;
    }

    @Override
    public void close() {

        // clean up listeners
        for (Mqtt3TopicListener listener : listeners) {
            delegate.getBroker().removeListener(listener);
        }

        connector.unbind();
        if(stopServiceOnClose) {
            connector.stop();
        }
    }

    @Override
    public void publish(String topic, int qos, byte[] payload) throws IOException {
        delegate.getBroker().publish(topic, qos, payload);
    }

    @Override
    public void start() {
        delegate.getBroker().start();
    }

    @Override
    public void stop() throws InterruptedException {
        delegate.getBroker().stop();
    }

    @Override
    public void addListener(Mqtt3TopicListener listener) {
        delegate.getBroker().addListener(listener);

        listeners.add(listener);
    }

    @Override
    public void removeListener(Mqtt3TopicListener listener) {
        delegate.getBroker().removeListener(listener);

        listeners.remove(listener);
    }

    @Override
    public String getHost() {
        return delegate.getBroker().getAddress().getHostName();
    }

    @Override
    public int getPort() {
        return delegate.getBroker().getAddress().getPort();
    }

}
