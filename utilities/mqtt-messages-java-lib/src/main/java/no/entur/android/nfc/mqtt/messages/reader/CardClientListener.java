package no.entur.android.nfc.mqtt.messages.reader;

import java.util.List;

public interface CardClientListener {

    void onCardLost();

    void onCardPresent(Reader client, List<String> technologies, byte[] atr, byte[] historicalBytes, byte[] uid);
}
