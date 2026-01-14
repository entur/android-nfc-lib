package no.entur.android.nfc.external.hwb.reader;

import java.util.Date;
import java.util.UUID;

import hwb.utilities.device.deviceId.diagnostics.DiagnosticsSchema;
import hwb.utilities.device.diagnostics.request.RequestSchema;
import no.entur.android.nfc.mqtt.messages.JsonResponseMqttMessage;
import no.entur.android.nfc.mqtt.messages.reader.ReaderPresentMessageConverter;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedResponseMessage;

public class HwbReaderMessageConverter implements ReaderPresentMessageConverter<String, HwbReaderContext> {

    @Override
    public HwbReaderPresentSynchronizedRequestMessageRequest createReaderPresentRequestMessage(HwbReaderContext context) {
        RequestSchema schema = new RequestSchema();
        schema.setEventTimestamp(new Date());
        schema.setTraceId(UUID.randomUUID());

        return new HwbReaderPresentSynchronizedRequestMessageRequest(schema, context.getDeviceId());
    }

    @Override
    public HwbReaderPresentSynchronizedResponseMessage createReaderPresentResponseMessage(SynchronizedResponseMessage<String> message, HwbReaderContext context) {
        if(message instanceof JsonResponseMqttMessage) {
            JsonResponseMqttMessage jsonResponseMqttMessage = (JsonResponseMqttMessage)message;
            Object payload = jsonResponseMqttMessage.getPayload();
            if(payload instanceof DiagnosticsSchema) {
                return new HwbReaderPresentSynchronizedResponseMessage((DiagnosticsSchema) payload, true);
            }
            throw new IllegalArgumentException("Unknown response payload type " + payload.getClass().getName());
        }
        throw new IllegalArgumentException("Unknown response message type " + message.getClass().getName());
    }
}
