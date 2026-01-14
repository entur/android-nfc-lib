package no.entur.android.nfc.mqtt.messages.card;

import java.util.List;

public interface CardContext {

    List<String> getTechnologies();
    byte[] getAtr();

    byte[] getHistoricalBytes();

    byte[] getUid();

}
