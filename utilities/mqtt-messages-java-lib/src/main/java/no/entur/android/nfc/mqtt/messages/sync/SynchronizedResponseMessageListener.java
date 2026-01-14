package no.entur.android.nfc.mqtt.messages.sync;

public interface SynchronizedResponseMessageListener<T> {

    void onResponseMessage(SynchronizedResponseMessage<T> message);

}
