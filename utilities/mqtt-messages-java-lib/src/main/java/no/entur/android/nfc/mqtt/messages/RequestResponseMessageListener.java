package no.entur.android.nfc.mqtt.messages;

public interface RequestResponseMessageListener<T> {

    void onMessage(RequestResponseMessage<T> message);

}
