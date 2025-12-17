package no.entur.android.nfc.external.hid.card;

import java.util.ArrayList;
import java.util.List;

import no.entur.android.nfc.external.hid.dto.atr210.ApduCommand;
import no.entur.android.nfc.external.hid.dto.atr210.NfcAdpuTransmitRequest;
import no.entur.android.nfc.external.hid.dto.atr210.NfcAdpuTransmitResponse;
import no.entur.android.nfc.mqtt.messages.JsonResponseMqttMessage;
import no.entur.android.nfc.mqtt.messages.card.CardAdpuMessageConverter;
import no.entur.android.nfc.mqtt.messages.card.CardAdpuSynchronizedRequestMessageRequest;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedResponseMessage;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;

public class Atr210CardMessageConverter implements CardAdpuMessageConverter<String, Atr210CardContext> {

    @Override
    public CardAdpuSynchronizedRequestMessageRequest<String, NfcAdpuTransmitRequest> createCardAdpuRequestMessage(byte[] message, Atr210CardContext context) {
        NfcAdpuTransmitRequest schema = new NfcAdpuTransmitRequest();
        List<ApduCommand> commands = new ArrayList<>();

        // do not set expected status (it is optional)
        ApduCommand command = new ApduCommand();
        command.setFrame(ByteArrayHexStringConverter.byteArrayToHexString(message));
        // not relevant here
        command.setCommandId(0);

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
                String topic = "txpt/ticketreader/" + context.getProviderId() + "/nfc/hf/apdu/response";
                return new Atr210CardAdpuSynchronizedResponseMessage(topic, (NfcAdpuTransmitResponse) payload);
            }
            throw new IllegalArgumentException("Unknown response payload type " + payload.getClass().getName());
        }
        throw new IllegalArgumentException("Unknown response message type " + message.getClass().getName());
    }

}
