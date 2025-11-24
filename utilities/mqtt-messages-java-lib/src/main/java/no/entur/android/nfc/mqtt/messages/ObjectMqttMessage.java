package no.entur.android.nfc.mqtt.messages;

public interface ObjectMqttMessage<P> {

    P getPayload();

    String getTopic();



}
