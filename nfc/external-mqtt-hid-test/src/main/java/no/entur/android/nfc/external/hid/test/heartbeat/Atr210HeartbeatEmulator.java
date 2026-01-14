package no.entur.android.nfc.external.hid.test.heartbeat;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.Date;

import no.entur.android.nfc.external.hid.dto.atr210.heartbeat.HeartbeatResponse;
import no.entur.android.nfc.external.hid.test.Atr210MessageSequence;
import no.entur.android.nfc.external.mqtt.test.MqttBrokerServiceConnection;

/**
 *
 * Emulate an ATR 210 NFC reader connected via MQTT.
 *
 */

public class Atr210HeartbeatEmulator implements HeartbeatTimer.HeartbeatListener, Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Atr210HeartbeatEmulator.class);

    protected final HeartbeatResponse heartbeatResponse;

    protected final MqttBrokerServiceConnection mqttBrokerServiceConnection;

    protected final String topic;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected final HeartbeatTimer timer;

    protected final Atr210MessageSequence sequence;

    public Atr210HeartbeatEmulator(HeartbeatResponse heartbeatResponse, MqttBrokerServiceConnection mqttBrokerServiceConnection, Atr210MessageSequence sequence) {
        this.heartbeatResponse = heartbeatResponse;
        this.mqttBrokerServiceConnection = mqttBrokerServiceConnection;

        this.topic = "itxpt/inventory/providers/itxpt.ticketreader." + heartbeatResponse.getDeviceType() + "." + heartbeatResponse.getDeviceId() + "/heartbeat/relative";
        this.sequence = sequence;

        long delay = (long)(heartbeatResponse.getNextHeartbeatWithinSeconds() * 1000);

        this.timer = new HeartbeatTimer(delay, this);
    }

    @Override
    public void onHeartbeat() {
        LOGGER.debug("Send heartbeat for " +  heartbeatResponse.getDeviceType() + " " + heartbeatResponse.getDeviceId() + " -> " + topic);
        try {
            heartbeatResponse.setSequence(sequence.next());
            heartbeatResponse.setTimestamp(new Date().toString());

            byte[] payload = objectMapper.writeValueAsBytes(heartbeatResponse);

            mqttBrokerServiceConnection.publish(topic, 1, payload);
        } catch (Exception e) {
            LOGGER.error("Problem sending heartbeat", e);
        }
    }

    public void start() {
        timer.schedule();
    }

    public void stop() {
        timer.cancel();
    }

    public void close() {
        timer.close();
    }
}
