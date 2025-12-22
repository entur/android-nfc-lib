package no.entur.android.nfc.external.hid.test.configuration;

import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import no.entur.android.nfc.external.hid.dto.atr210.NfcConfiguationRequest;
import no.entur.android.nfc.external.hid.dto.atr210.NfcConfiguationResponse;
import no.entur.android.nfc.external.hid.dto.atr210.ReadersStatusResponse;
import no.entur.android.nfc.external.hid.test.Atr210MessageSequence;
import no.entur.android.nfc.external.hid.test.heartbeat.Atr210HeartbeatEmulator;
import no.entur.android.nfc.external.mqtt.test.MqttBrokerServiceConnection;
import no.entur.android.nfc.external.mqtt3.broker.Mqtt3TopicListener;
import no.entur.android.nfc.external.mqtt3.broker.Mqtt3WebSocketBroker;

public class Atr210ConfigurationEmulator implements Mqtt3TopicListener, Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Atr210ConfigurationEmulator.class);

    protected final MqttBrokerServiceConnection mqttBrokerServiceConnection;

    protected final String nfcReadersConfigurationRequestTopic; // itxpt/ticketreader/{PROVIDER_ID}/nfc/readers/configuration/request
    protected final String nfcReadersConfigurationTopic; // itxpt/ticketreader/{PROVIDER_ID}/nfc/readers/configuration

    protected final String nfcReadersRequestTopic; // itxpt/ticketreader/{PROVIDER_ID}/nfc/readers/request
    protected final String nfcReadersTopic; // itxpt/ticketreader/{PROVIDER_ID}/nfc/readers

    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected final Atr210MessageSequence sequence;

    protected boolean closed = false;

    protected String reader;

    protected Atr210ConfigurationListener listener;

    public Atr210ConfigurationEmulator(String deviceType, String deviceId, String reader, MqttBrokerServiceConnection mqttBrokerServiceConnection, Atr210MessageSequence sequence, Atr210ConfigurationListener listener) {
        this.mqttBrokerServiceConnection = mqttBrokerServiceConnection;

        String prefix = "itxpt/ticketreader/itxpt.ticketreader." + deviceType + "." + deviceId;

        this.nfcReadersConfigurationRequestTopic = prefix + "/nfc/readers/configuration/request";
        this.nfcReadersConfigurationTopic = prefix + "/nfc/readers/configuration";
        this.nfcReadersRequestTopic = prefix + "/nfc/readers/request";
        this.nfcReadersTopic = prefix + "/nfc/readers";
        this.sequence = sequence;

        this.reader = reader;
        this.listener = listener;

        mqttBrokerServiceConnection.addListener(this);
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
        if(topic.equals(nfcReadersConfigurationRequestTopic)) {
            try {

                NfcConfiguationRequest request = objectMapper.readValue(bytes, NfcConfiguationRequest.class);

                NfcConfiguationResponse response;
                if(request.isEmpty()) {
                    response = listener.onGetConfiguration();
                } else {
                    response = listener.onSetConfiguration(request);
                }

                response.setSequence(sequence.next());
                response.setTimestamp(new Date().toString());

                byte[] payload = objectMapper.writeValueAsBytes(response);

                mqttBrokerServiceConnection.publish(nfcReadersConfigurationTopic, 1, payload);
            } catch (Exception e) {
                LOGGER.error("Problem replying to topic " + topic, e);
            }
        } else if(topic.equals(nfcReadersRequestTopic)) {
            try {

                ReadersStatusResponse response = listener.onGetReaders();
                response.setSequence(sequence.next());
                response.setTimestamp(new Date().toString());
                byte[] payload = objectMapper.writeValueAsBytes(response);

                mqttBrokerServiceConnection.publish(nfcReadersTopic, 1, payload);
            } catch (Exception e) {
                LOGGER.error( "Problem replying to topic " + topic, e);
            }
        }

    }


}
