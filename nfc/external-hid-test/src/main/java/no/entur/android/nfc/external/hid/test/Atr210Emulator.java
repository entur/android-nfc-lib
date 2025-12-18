package no.entur.android.nfc.external.hid.test;

import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.entur.android.nfc.external.mqtt3.broker.Mqtt3TopicListener;
import no.entur.android.nfc.external.mqtt3.broker.Mqtt3WebSocketBroker;
import no.entur.android.nfc.external.mqtt.test.MqttBrokerServiceConnection;


public class Atr210Emulator implements Mqtt3TopicListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(Atr210Emulator.class);

	protected MqttBrokerServiceConnection mqttBrokerServiceConnection;

    public Atr210Emulator(MqttBrokerServiceConnection mqttBrokerServiceConnection) {
        this.mqttBrokerServiceConnection = mqttBrokerServiceConnection;
    }

    @Override
    public void onPublish(Mqtt3WebSocketBroker broker, WebSocket source, String topic, byte[] bytes) {

        // itxpt/inventory/providers/{PROVIDER_ID}/heartbeat/relative



    }
}
