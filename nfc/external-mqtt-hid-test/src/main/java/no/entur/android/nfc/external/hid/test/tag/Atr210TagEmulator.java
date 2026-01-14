package no.entur.android.nfc.external.hid.test.tag;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import no.entur.android.nfc.external.hid.dto.atr210.ApduCommand;
import no.entur.android.nfc.external.hid.dto.atr210.ApduResponse;
import no.entur.android.nfc.external.hid.dto.atr210.HfReaderStatusResponse;
import no.entur.android.nfc.external.hid.dto.atr210.NfcAdpuTransmitRequest;
import no.entur.android.nfc.external.hid.dto.atr210.NfcAdpuTransmitResponse;
import no.entur.android.nfc.external.hid.dto.atr210.ReaderStatus;
import no.entur.android.nfc.external.hid.intent.NfcCardStatus;
import no.entur.android.nfc.external.hid.test.Atr210MessageSequence;
import no.entur.android.nfc.external.hid.test.heartbeat.Atr210HeartbeatEmulator;
import no.entur.android.nfc.external.mqtt.test.MqttBrokerServiceConnection;
import no.entur.android.nfc.external.mqtt3.broker.Mqtt3TopicListener;
import no.entur.android.nfc.external.mqtt3.broker.Mqtt3WebSocketBroker;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;
import no.entur.android.nfc.wrapper.test.tech.transceive.MockTransceive;

/**
 *
 * Emulate a tag presented on an ATR 210 NFC reader.
 *
 */

public class Atr210TagEmulator implements Mqtt3TopicListener, Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Atr210HeartbeatEmulator.class);

    protected final MqttBrokerServiceConnection mqttBrokerServiceConnection;

    protected final String statusTopic;
    protected final String adpuTransmitTopic;
    protected final String adpuResponseTopic;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected final Atr210MessageSequence sequence;

    protected boolean closed = false;

    protected String reader;

    protected Atr210MqttTag tag;

    public Atr210TagEmulator(String deviceType, String deviceId, String reader, MqttBrokerServiceConnection mqttBrokerServiceConnection, Atr210MessageSequence sequence) {
        this.mqttBrokerServiceConnection = mqttBrokerServiceConnection;

        this.statusTopic = "itxpt/ticketreader/itxpt.ticketreader." + deviceType + "." + deviceId + "/nfc/readers/status";

        this.adpuTransmitTopic = "itxpt/ticketreader/itxpt.ticketreader." + deviceType + "." + deviceId + "/nfc/hf/apdu/transmit";
        this.adpuResponseTopic = "itxpt/ticketreader/itxpt.ticketreader." + deviceType + "." + deviceId + "/nfc/hf/apdu/response";

        this.sequence = sequence;

        this.reader = reader;

        mqttBrokerServiceConnection.addListener(this);
    }

    public void tagLost() {
        if(tag != null) {
            this.tag = null;

            sendTagNotPresent();
        }
    }

    public void tagPresent(Atr210MqttTag tag) {
        this.tag = tag;

        String atrString = ByteArrayHexStringConverter.toHexString(tag.getAtr());
        String tagIdString = ByteArrayHexStringConverter.toHexString(tag.getTagId());

        sendTagPresent(atrString, tagIdString);
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

        LOGGER.debug("Send tag present -> " + statusTopic);
        try {

            byte[] payload = objectMapper.writeValueAsBytes(response);

            mqttBrokerServiceConnection.publish(statusTopic, 1, payload);
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

        LOGGER.debug("Send tag not present -> " + statusTopic);
        try {

            byte[] payload = objectMapper.writeValueAsBytes(response);

            mqttBrokerServiceConnection.publish(statusTopic, 1, payload);
        } catch (Exception e) {
            LOGGER.error( "Problem sending tag not present", e);
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

        if(topic.equals(adpuTransmitTopic)) {

            Atr210MqttTag tag = this.tag; // defensive copy for multithreading
            if(tag == null) {
                LOGGER.warn("No tag for transcieve");
                return;
            }

            try {
                NfcAdpuTransmitRequest request = objectMapper.readValue(bytes, NfcAdpuTransmitRequest.class);

                LOGGER.info("Got transmit request with " + request.getCommands().size() + " commands");

                MockTransceive tagTechnology = tag.getTagTechnology();

                List<ApduResponse> result = new ArrayList<>();

                for (ApduCommand command : request.getCommands()) {

                    byte[] frame = ByteArrayHexStringConverter.hexStringToByteArray(command.getFrame());

                    byte[] responseFrame = tagTechnology.transceive(frame, true);

                    ApduResponse response = new ApduResponse();
                    response.setResponse(ByteArrayHexStringConverter.toHexString(responseFrame));
                    response.setFrame(command.getFrame());
                    response.setCommandId(command.getCommandId());

                    result.add(response);

                    if(responseFrame == null) {
                        LOGGER.info("Empty response for command " + command.getCommandId());

                        break;
                    }
                }

                NfcAdpuTransmitResponse response = new NfcAdpuTransmitResponse(result);
                response.setSequence(sequence.next());
                response.setTimestamp(new Date().toString());

                byte[] payload = objectMapper.writeValueAsBytes(response);

                mqttBrokerServiceConnection.publish(adpuResponseTopic, 1, payload);
            } catch (Exception e) {
                LOGGER.error("Problem replying to topic " + topic, e);
            }
        }

    }

}
