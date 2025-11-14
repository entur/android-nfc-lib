package no.entur.android.nfc.mqtt.messages;

public interface ResponseMessageListener<T> {

    void onResponseMessage(ResponseMessage<T> message);

}
