package no.entur.android.nfc.mqtt.messages;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;

public interface JsonRequestMqttMessage<P> extends ObjectMqttMessage<P> {

    void write(JsonGenerator generator) throws IOException;


}
