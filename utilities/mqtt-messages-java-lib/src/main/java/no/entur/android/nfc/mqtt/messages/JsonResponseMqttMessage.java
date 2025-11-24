package no.entur.android.nfc.mqtt.messages;

import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;

public interface JsonResponseMqttMessage<P> extends ObjectMqttMessage<P> {

    void read(JsonParser parser) throws IOException;


}
