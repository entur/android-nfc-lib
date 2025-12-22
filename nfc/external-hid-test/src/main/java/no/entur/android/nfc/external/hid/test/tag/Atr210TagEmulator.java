package no.entur.android.nfc.external.hid.test.tag;

import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.Date;

import no.entur.android.nfc.external.hid.dto.atr210.HfReaderStatusResponse;
import no.entur.android.nfc.external.hid.dto.atr210.ReaderStatus;
import no.entur.android.nfc.external.hid.intent.NfcCardStatus;
import no.entur.android.nfc.external.hid.test.Atr210MessageSequence;
import no.entur.android.nfc.external.hid.test.heartbeat.Atr210HeartbeatEmulator;
import no.entur.android.nfc.external.mqtt.test.MqttBrokerServiceConnection;

public class Atr210TagEmulator implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Atr210HeartbeatEmulator.class);

    protected final MqttBrokerServiceConnection mqttBrokerServiceConnection;

    protected final String topic;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected final Atr210MessageSequence sequence;

    protected boolean closed = false;

    protected String reader;

    public Atr210TagEmulator(String deviceType, String deviceId, String reader, MqttBrokerServiceConnection mqttBrokerServiceConnection, Atr210MessageSequence sequence) {
        this.mqttBrokerServiceConnection = mqttBrokerServiceConnection;

        this.topic = "itxpt/ticketreader/itxpt.ticketreader." + deviceType + "." + deviceId + "/nfc/readers/status";
        this.sequence = sequence;

        this.reader = reader;
    }

    public void sendTagPresent(String atr, String tagId) {

        HfReaderStatusResponse response = new HfReaderStatusResponse();

        response.setSequence(sequence.next());
        response.setTimestamp(new Date().toString());

        ReaderStatus readerStatus = new ReaderStatus();
        readerStatus.setCardATR(atr);
        readerStatus.setId(reader);
        readerStatus.setCardCSN(tagId);

        readerStatus.add(NfcCardStatus.CHANGED);
        readerStatus.add(NfcCardStatus.PRESENT);

        response.add(readerStatus);

        LOGGER.debug("Send tag present -> " + topic);
        try {

            byte[] payload = objectMapper.writeValueAsBytes(response);

            mqttBrokerServiceConnection.publish(topic, 1, payload);
        } catch (Exception e) {
            LOGGER.error( "Problem sending tag present", e);
        }
    }

    public void sendTagNotPresent() {

        HfReaderStatusResponse response = new HfReaderStatusResponse();

        response.setSequence(sequence.next());
        response.setTimestamp(new Date().toString());

        ReaderStatus readerStatus = new ReaderStatus();
        readerStatus.setId(reader);

        readerStatus.add(NfcCardStatus.CHANGED);
        readerStatus.add(NfcCardStatus.EMPTY);

        response.add(readerStatus);

        LOGGER.debug("Send tag not present -> " + topic);
        try {

            byte[] payload = objectMapper.writeValueAsBytes(response);

            mqttBrokerServiceConnection.publish(topic, 1, payload);
        } catch (Exception e) {
            LOGGER.error( "Problem sending tag not present", e);
        }

        closed = true;
    }

    public void close() {
        closed = true;
    }
}
