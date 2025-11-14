package no.entur.android.nfc.mqtt.messages.reader;

public class CardLeftFieldMessage<T> extends CardAdpuResponseMessage<T> {

    public CardLeftFieldMessage(byte[] adpu, T id) {
        super(adpu, id);
    }
}
