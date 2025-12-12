package no.entur.android.nfc.external.hwb.card;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import no.entur.android.nfc.mqtt.messages.JsonResponseMqttMessage;
import no.entur.android.nfc.mqtt.messages.card.CardAdpuMessageConverter;
import no.entur.android.nfc.mqtt.messages.card.CardAdpuSynchronizedRequestMessageRequest;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedResponseMessage;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;

public class Atr210CardMessageConverter implements CardAdpuMessageConverter<UUID, Atr210CardContext> {

    @Override
    public CardAdpuSynchronizedRequestMessageRequest<UUID, TransmitSchema> createCardAdpuRequestMessage(byte[] message, Atr210CardContext context) {
        TransmitSchema schema = new TransmitSchema();
        schema.setApduType(context.getApduType());
        schema.setDeviceId(context.getDeviceId());
        schema.setTransceiveId(UUID.randomUUID());
        schema.setEventTimestamp(new Date());
        schema.setTraceId(context.getTraceId());
        List<Command> commands = new ArrayList<>();

        // do not set expected status (it is optional)
        Command command = new Command();
        command.setFrame(ByteArrayHexStringConverter.byteArrayToHexString(message));
        // not relevant here
        command.setCommandId(0);

        commands.add(command);
        schema.setCommand(commands);
        return new Atr210CardAdpuSynchronizedRequestMessage(schema);
    }

    @Override
    public Atr210CardAdpuSynchronizedResponseMessage createCardAdpuResponseMessage(SynchronizedResponseMessage<UUID> message, Atr210CardContext context) {
        if(message instanceof JsonResponseMqttMessage) {
            JsonResponseMqttMessage jsonResponseMqttMessage = (JsonResponseMqttMessage)message;
            Object payload = jsonResponseMqttMessage.getPayload();
            if(payload instanceof ReceiveSchema) {
                return new Atr210CardAdpuSynchronizedResponseMessage((ReceiveSchema) payload);
            }
            throw new IllegalArgumentException("Unknown response payload type " + payload.getClass().getName());
        }
        throw new IllegalArgumentException("Unknown response message type " + message.getClass().getName());
    }

}
