package no.entur.android.nfc.external.hid.test.barcode;

import android.util.Base64;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.Date;

import no.entur.android.nfc.external.hid.dto.atr210.TicketRequest;
import no.entur.android.nfc.external.hid.dto.atr210.TicketResponse;
import no.entur.android.nfc.external.hid.test.Atr210MessageSequence;
import no.entur.android.nfc.external.hid.test.heartbeat.Atr210HeartbeatEmulator;
import no.entur.android.nfc.external.mqtt.test.MqttBrokerServiceConnection;
import no.entur.android.nfc.external.mqtt3.broker.Mqtt3TopicListener;
import no.entur.android.nfc.external.mqtt3.broker.Mqtt3WebSocketBroker;

/**
 *
 * Emulate a tag presented on an ATR 210 NFC reader.
 *
 */

public class Atr210BarcodeEmulator implements Mqtt3TopicListener, Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Atr210HeartbeatEmulator.class);

    protected final MqttBrokerServiceConnection mqttBrokerServiceConnection;

    protected final String requestTopic;
    protected final String responseTopic;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected final Atr210MessageSequence sequence;

    protected boolean closed = false;

    protected String reader;

    protected TicketResponse response;

    public Atr210BarcodeEmulator(String deviceType, String deviceId, String reader, MqttBrokerServiceConnection mqttBrokerServiceConnection, Atr210MessageSequence sequence) {
        this.mqttBrokerServiceConnection = mqttBrokerServiceConnection;

        this.requestTopic = "itxpt/ticketreader/itxpt.ticketreader." + deviceType + "." + deviceId + "/request/validation";
        this.responseTopic = "itxpt/ticketreader/itxpt.ticketreader." + deviceType + "." + deviceId + "/response/validation";
        this.sequence = sequence;

        this.reader = reader;

        mqttBrokerServiceConnection.addListener(this);
    }

    public void sendTicketRequest(byte[] barcode) {

        TicketRequest response = new TicketRequest();

        response.setSequence(sequence.next());
        response.setTimestamp(new Date().toString());
        response.setBarcode(Base64.encodeToString(barcode, Base64.NO_WRAP));

        LOGGER.debug("Send barcode -> " + requestTopic);
        try {
            byte[] payload = objectMapper.writeValueAsBytes(response);

            mqttBrokerServiceConnection.publish(requestTopic, 1, payload);
        } catch (Exception e) {
            LOGGER.error( "Problem sending barcode", e);
        }
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

        if(topic.equals(responseTopic)) {
            try {
                TicketResponse ticketResponse = objectMapper.readValue(bytes, TicketResponse.class);

                LOGGER.info("Got " + (ticketResponse.isValid() ? "valid" : "invalid") + " ticket response (led: " + ticketResponse.getLed() + ", sound " + ticketResponse.getSound() + ")");

                this.response = ticketResponse;
            } catch (Exception e) {
                LOGGER.error("Problem replying to topic " + topic, e);
            }
        }

    }

    public TicketResponse getResponse() {
        return response;
    }
}
