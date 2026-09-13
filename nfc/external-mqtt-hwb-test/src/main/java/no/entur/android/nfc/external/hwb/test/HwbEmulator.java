package no.entur.android.nfc.external.hwb.test;

import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.entur.android.nfc.external.hwb.test.heartbeat.HwbHeartbeatEmulator;
import no.entur.android.nfc.external.hwb.test.tag.HwbTagEmulator;
import no.entur.android.nfc.external.mqtt.test.MqttBrokerServiceConnection;
import no.entur.android.nfc.external.mqtt3.broker.Mqtt3TopicListener;
import no.entur.android.nfc.external.mqtt3.broker.Mqtt3WebSocketBroker;


public class HwbEmulator implements Mqtt3TopicListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(HwbEmulator.class);

	protected MqttBrokerServiceConnection mqttBrokerServiceConnection;

    protected final HwbHeartbeatEmulator heartbeatEmulator;

    protected final HwbTagEmulator tagEmulator;

    public HwbEmulator(MqttBrokerServiceConnection mqttBrokerServiceConnection, HwbHeartbeatEmulator heartbeatEmulator, HwbTagEmulator tagEmulator) {
        this.mqttBrokerServiceConnection = mqttBrokerServiceConnection;
        this.heartbeatEmulator = heartbeatEmulator;
        this.tagEmulator = tagEmulator;
    }

    @Override
    public void onPublish(Mqtt3WebSocketBroker broker, WebSocket source, String topic, byte[] bytes) {

        // itxpt/inventory/providers/{PROVIDER_ID}/heartbeat/relative



    }
}
