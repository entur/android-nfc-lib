package no.entur.android.nfc.external.hwb.reader;

import java.util.Date;
import java.util.UUID;

import hwb.utilities.device.deviceId.diagnostics.DiagnosticsSchema;
import hwb.utilities.device.diagnostics.request.RequestSchema;
import no.entur.android.nfc.mqtt.messages.JsonResponseMqttMessage;
import no.entur.android.nfc.mqtt.messages.reader.ReaderPresentMessageConverter;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedResponseMessage;

public class Atr210ReaderMessageConverter implements ReaderPresentMessageConverter<String, Atr210ReaderContext> {

    @Override
    public Atr210ReaderPresentSynchronizedRequestMessageRequest createReaderPresentRequestMessage(Atr210ReaderContext context) {
        RequestSchema schema = new RequestSchema();
        schema.setEventTimestamp(new Date());
        schema.setTraceId(UUID.randomUUID());

        return new Atr210ReaderPresentSynchronizedRequestMessageRequest(schema, context.getDeviceId());
    }

    @Override
    public Atr210ReaderPresentSynchronizedResponseMessage createReaderPresentResponseMessage(SynchronizedResponseMessage<String> message, Atr210ReaderContext context) {
        if(message instanceof JsonResponseMqttMessage) {
            JsonResponseMqttMessage jsonResponseMqttMessage = (JsonResponseMqttMessage)message;
            Object payload = jsonResponseMqttMessage.getPayload();
            if(payload instanceof DiagnosticsSchema) {
                return new Atr210ReaderPresentSynchronizedResponseMessage((DiagnosticsSchema) payload, true);
            }
            throw new IllegalArgumentException("Unknown response payload type " + payload.getClass().getName());
        }
        throw new IllegalArgumentException("Unknown response message type " + message.getClass().getName());
    }
}
