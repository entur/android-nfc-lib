package no.entur.android.nfc.external.hid.card;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import no.entur.android.nfc.external.hid.dto.atr210.ApduCommand;
import no.entur.android.nfc.external.hid.dto.atr210.NfcAdpuTransmitRequest;
import no.entur.android.nfc.external.hid.dto.atr210.NfcAdpuTransmitResponse;
import no.entur.android.nfc.mqtt.messages.JsonResponseMqttMessage;
import no.entur.android.nfc.mqtt.messages.card.CardAdpuMessageConverter;
import no.entur.android.nfc.mqtt.messages.card.CardAdpuSynchronizedRequestMessageRequest;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedResponseMessage;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;

public class Atr210CardMessageConverter implements CardAdpuMessageConverter<String, Atr210CardContext> {

    private static final AtomicInteger COMMAND_ID = new AtomicInteger();

    /**
     * @return a non-zero command id (1 - 65535)
     */

    protected static int nextCommandId() {
        int id;
        do {
            id = COMMAND_ID.incrementAndGet() & 0xFFFF;
        } while (id == 0);
        return id;
    }

    @Override
    public CardAdpuSynchronizedRequestMessageRequest<String, NfcAdpuTransmitRequest> createCardAdpuRequestMessage(byte[] message, Atr210CardContext context) {
        NfcAdpuTransmitRequest schema = new NfcAdpuTransmitRequest();
        List<ApduCommand> commands = new ArrayList<>();

        // do not set expected status (it is optional)
        ApduCommand command = new ApduCommand();
        command.setFrame(ByteArrayHexStringConverter.byteArrayToHexString(message));
        // unique per request, so that a late response can be told apart from the response to the next request
        command.setCommandId(nextCommandId());

        commands.add(command);
        schema.setCommands(commands);

        String requestTopic = "itxpt/ticketreader/" + context.getProviderId() + "/nfc/hf/apdu/transmit";
        String responseTopic = "itxpt/ticketreader/" + context.getProviderId() + "/nfc/hf/apdu/response";

        return new Atr210CardAdpuSynchronizedRequestMessage(schema, requestTopic, responseTopic);
    }

    @Override
    public Atr210CardAdpuSynchronizedResponseMessage createCardAdpuResponseMessage(SynchronizedResponseMessage<String> message, Atr210CardContext context) {
        if(message instanceof JsonResponseMqttMessage) {
            JsonResponseMqttMessage jsonResponseMqttMessage = (JsonResponseMqttMessage)message;
            Object payload = jsonResponseMqttMessage.getPayload();
            if(payload instanceof NfcAdpuTransmitResponse) {
                String topic = "itxpt/ticketreader/" + context.getProviderId() + "/nfc/hf/apdu/response";
                return new Atr210CardAdpuSynchronizedResponseMessage(topic, (NfcAdpuTransmitResponse) payload);
            }
            throw new IllegalArgumentException("Unknown response payload type " + payload.getClass().getName());
        }
        if(message instanceof Atr210CardAdpuSynchronizedResponseMessage) {
            return (Atr210CardAdpuSynchronizedResponseMessage) message;
        }
        throw new IllegalArgumentException("Unknown response message type " + message.getClass().getName());
    }

}
