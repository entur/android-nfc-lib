package no.entur.android.nfc.external.hwb.test.tag;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import hwb.utilities.validators.nfc.CardContent;
import hwb.utilities.validators.nfc.NfcSchema;
import hwb.utilities.validators.nfc.apdu.deviceId.transmit.Command;
import hwb.utilities.validators.nfc.apdu.deviceId.transmit.TransmitSchema;
import hwb.utilities.validators.nfc.apdu.receive.ReceiveSchema;
import hwb.utilities.validators.nfc.apdu.receive.Result;
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

public class HwbTagEmulator implements Mqtt3TopicListener, Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(HwbTagEmulator.class);

    protected final MqttBrokerServiceConnection mqttBrokerServiceConnection;

    protected final String statusTopic;
    protected final String adpuResponseTopic;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected boolean closed = false;

    protected HwbMqttTag tag;

    public HwbTagEmulator(MqttBrokerServiceConnection mqttBrokerServiceConnection) {
        this.mqttBrokerServiceConnection = mqttBrokerServiceConnection;

        this.statusTopic = "/validators/nfc";

        this.adpuResponseTopic = "/validators/nfc/apdu/receive";

        mqttBrokerServiceConnection.addListener(this);
    }

    public void tagLost() {
        if(tag != null) {
            this.tag = null;

            // sendTagNotPresent not supported
        }
    }

    public void tagPresent(HwbMqttTag tag) {
        this.tag = tag;

        List<CardContent> cardContent = tag.getCardContent();
        String token = tag.getToken();
        String travelCardNumber = tag.getTravelCardNumber();
        String deviceId = tag.getDeviceId();

        sendTagPresent(deviceId, token, travelCardNumber, cardContent);
    }

    public void sendTagPresent(String deviceId, String token, String travelCardNumber, List<CardContent> cardContent) {
        NfcSchema response = new NfcSchema();

        response.setTraceId(UUID.randomUUID());
        response.setEventTimestamp(new Date());

        response.setDeviceId(deviceId);
        response.setToken(token);
        response.setTravelCardNumber(travelCardNumber);
        response.setCardContent(cardContent);

        LOGGER.debug("Send tag present -> " + statusTopic);
        try {

            byte[] payload = objectMapper.writeValueAsBytes(response);

            mqttBrokerServiceConnection.publish(statusTopic, 1, payload);
        } catch (Exception e) {
            LOGGER.error( "Problem sending tag present", e);
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

        if(tag == null) {
            LOGGER.warn("No tag for transcieve");
            return;
        }

        if(topic.equals("/validators/nfc/apdu/" + this.tag.getDeviceId() + "/transmit")) {
            try {

                TransmitSchema request = objectMapper.readValue(bytes, TransmitSchema.class);

                LOGGER.info("Got transmit request with " + request.getCommand().size() + " commands");

                MockTransceive tagTechnology = tag.getTagTechnology();

                List<Result> result = new ArrayList<>();

                for (Command command : request.getCommand()) {

                    byte[] frame = ByteArrayHexStringConverter.hexStringToByteArray(command.getFrame());

                    byte[] responseFrame = tagTechnology.transceive(frame, true);

                    Result response = new Result();
                    response.setFrame(command.getFrame());
                    response.setCommandId(command.getCommandId());

                    result.add(response);

                    if(responseFrame == null) {
                        LOGGER.info("Empty response for command " + command.getCommandId());

                        break;
                    }
                }

                ReceiveSchema response = new ReceiveSchema();
                response.setResult(result);
                response.setEventTimestamp(new Date());
                response.setTraceId(request.getTraceId());
                response.setTransceiveId(request.getTransceiveId());

                switch(request.getApduType()) {
                    case DESFIRE: {
                        response.setApduType(ReceiveSchema.ApduType.DESFIRE);
                        break;
                    }
                    case ISO_7816: {
                        response.setApduType(ReceiveSchema.ApduType.ISO_7816);
                        break;
                    }
                }

                byte[] payload = objectMapper.writeValueAsBytes(response);

                mqttBrokerServiceConnection.publish(adpuResponseTopic, 1, payload);
            } catch (Exception e) {
                LOGGER.error("Problem replying to topic " + topic, e);
            }
        }

    }

}
