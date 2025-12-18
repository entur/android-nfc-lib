package no.entur.android.nfc.external.hid.test;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hwb.utilities.mqtt3.broker.Mqtt3TopicListener;
import hwb.utilities.mqtt3.broker.Mqtt3WebSocketBroker;
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
