package no.entur.android.nfc.external.hwb.test.heartbeat;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.Date;
import java.util.UUID;

import hwb.utilities.device.diagnostics.DiagnosticsSchema;
import no.entur.android.nfc.external.mqtt.test.MqttBrokerServiceConnection;
import no.entur.android.nfc.external.mqtt3.broker.Mqtt3TopicListener;
import no.entur.android.nfc.external.mqtt3.broker.Mqtt3WebSocketBroker;

/**
 *
 * Emulate a tag presented on an HWB NFC reader.
 *
 */

public class HwbHeartbeatEmulator implements Mqtt3TopicListener, Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(HwbHeartbeatEmulator.class);

    public interface HeartbeatSource {
        DiagnosticsSchema getHeartbeat(String deviceId);
    }

    public static class SimpleHeartbeatSource implements HeartbeatSource {

        @Override
        public DiagnosticsSchema getHeartbeat(String deviceId) {
            DiagnosticsSchema response = new DiagnosticsSchema();

            response.setDeviceId(deviceId);

            response.setEventTimestamp(new Date());
            response.setTraceId(UUID.randomUUID());

            return response;
        }
    }

    public static class FixedHeartbeatSource implements HeartbeatSource {

        private DiagnosticsSchema schema;

        @Override
        public DiagnosticsSchema getHeartbeat(String deviceId) {
            return schema;
        }
    }

    protected final MqttBrokerServiceConnection mqttBrokerServiceConnection;

    protected final String requestTopic = "/validators/barcode/request";
    protected final String responseTopic = "/validators/barcode";

    protected final ObjectMapper objectMapper = new ObjectMapper();
    protected final String deviceId;

    protected boolean closed = false;

    protected final HeartbeatSource heartbeatSource;

    public HwbHeartbeatEmulator(String deviceId, MqttBrokerServiceConnection mqttBrokerServiceConnection, HeartbeatSource heartbeatSource) {
        this.deviceId = deviceId;
        this.mqttBrokerServiceConnection = mqttBrokerServiceConnection;
        this.heartbeatSource = heartbeatSource;

        mqttBrokerServiceConnection.addListener(this);
    }

    public void sendDiagnostics() {
        DiagnosticsSchema response = new DiagnosticsSchema();

        response.setDeviceId(deviceId);

        response.setEventTimestamp(new Date());
        response.setTraceId(UUID.randomUUID());

        sendDiagnostics(response);
    }

    public void close() {
        mqttBrokerServiceConnection.removeListener(this);
        closed = true;
    }


    @Override
    public void onPublish(Mqtt3WebSocketBroker broker, WebSocket source, String topic, byte[] bytes) {
        if(closed) {
            return;
        }

        if(topic.equals(requestTopic)) {
            try {
                sendDiagnostics(heartbeatSource.getHeartbeat(deviceId));
            } catch (Exception e) {
                LOGGER.error("Problem replying to topic " + topic, e);
            }
        }

    }

    public void sendDiagnostics(DiagnosticsSchema barcode) {
        LOGGER.debug("Send heartbeat -> " + responseTopic);
        try {
            byte[] payload = objectMapper.writeValueAsBytes(barcode);

            mqttBrokerServiceConnection.publish(responseTopic, 1, payload);
        } catch (Exception e) {
            LOGGER.error( "Problem sending barcode", e);
        }
    }

}
