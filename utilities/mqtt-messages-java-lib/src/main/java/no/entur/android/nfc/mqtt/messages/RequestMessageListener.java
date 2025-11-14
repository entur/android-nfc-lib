package no.entur.android.nfc.mqtt.messages;

public interface RequestMessageListener<T> {

    void onRequestMessage(RequestMessage<T> message, ResponseMessageListener<T> listener);

}
