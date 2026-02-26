package no.entur.android.nfc.external.hwb.test.barcode;

import android.util.Base64;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.Date;
import java.util.UUID;

import hwb.utilities.validators.barcode.BarcodeSchema;
import no.entur.android.nfc.external.mqtt.test.MqttBrokerServiceConnection;

/**
 *
 * Emulate a tag presented on an HWB NFC reader.
 *
 */

public class HwbBarcodeEmulator implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(HwbBarcodeEmulator.class);

    protected final MqttBrokerServiceConnection mqttBrokerServiceConnection;

    protected final String requestTopic = "/validators/barcode";

    protected final ObjectMapper objectMapper = new ObjectMapper();
    protected final String deviceId;

    protected boolean closed = false;

    protected String reader;
    public HwbBarcodeEmulator(String deviceId, String reader, MqttBrokerServiceConnection mqttBrokerServiceConnection) {
        this.deviceId = deviceId;
        this.reader = reader;
        this.mqttBrokerServiceConnection = mqttBrokerServiceConnection;
    }

    public void sendBarcode(byte[] barcode) {
        BarcodeSchema response = new BarcodeSchema();

        response.setDeviceId(deviceId);

        response.setEventTimestamp(new Date());
        response.setTraceId(UUID.randomUUID());

        response.setBarcode(Base64.encodeToString(barcode, Base64.NO_WRAP));

        sendBarcode(response);
    }

    public void close() {
        closed = true;
    }

    public void sendBarcode(BarcodeSchema barcode) {
        LOGGER.debug("Send barcode -> " + requestTopic);
        try {
            byte[] payload = objectMapper.writeValueAsBytes(barcode);

            mqttBrokerServiceConnection.publish(requestTopic, 1, payload);
        } catch (Exception e) {
            LOGGER.error( "Problem sending barcode", e);
        }
    }

}
