package no.entur.android.nfc.websocket.messages;

import java.util.ArrayList;
import java.util.List;

public class CompositeNfcMessageListener implements NfcMessageListener {

    private List<NfcMessageListener> delegates = new ArrayList<>();

    @Override
    public void onMessage(NfcMessage message) {
        for (NfcMessageListener delegate : delegates) {
            delegate.onMessage(message);
        }
    }

    public boolean add(NfcMessageListener nfcMessageListener) {
        return delegates.add(nfcMessageListener);
    }

    public boolean remove(Object o) {
        return delegates.remove(o);
    }

    public void clear() {
        delegates.clear();
    }
}
