package no.entur.android.nfc.external.hwb;

import java.util.UUID;

import no.entur.android.nfc.mqtt.messages.JsonRequestMqttMessage;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedResponseMessage;

public class HwbRequestMqttResponseMessage implements SynchronizedResponseMessage<UUID>, JsonRequestMqttMessage {

    protected UUID uuid;
    protected String topic;

    protected Object payload;

    @Override
    public UUID getCorrelationId() {
        return uuid;
    }

    public Object getPayload() {
        return payload;
    }

    public String getTopic() {
        return topic;
    }
}
