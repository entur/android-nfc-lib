package no.entur.android.nfc.mqtt.messages;

import java.util.ArrayList;
import java.util.List;

public class CompositeRequestResponseMessageListener<T> implements RequestResponseMessageListener<T> {

    private List<RequestResponseMessageListener<T>> delegates = new ArrayList<>();

    @Override
    public void onMessage(RequestResponseMessage<T> message) {
        for (RequestResponseMessageListener<T> delegate : delegates) {
            delegate.onMessage(message);
        }
    }

    public boolean add(RequestResponseMessageListener<T> requestResponseMessageListener) {
        return delegates.add(requestResponseMessageListener);
    }

    public boolean remove(Object o) {
        return delegates.remove(o);
    }

    public void clear() {
        delegates.clear();
    }
}
