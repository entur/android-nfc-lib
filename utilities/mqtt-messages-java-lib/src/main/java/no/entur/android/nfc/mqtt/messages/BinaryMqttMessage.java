package no.entur.android.nfc.mqtt.messages;

public interface BinaryMqttMessage<P> {

    byte[] getBytes();

    String getTopic();

}
