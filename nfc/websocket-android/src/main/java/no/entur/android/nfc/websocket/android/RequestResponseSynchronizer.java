package no.entur.android.nfc.websocket.android;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import no.entur.android.nfc.websocket.messages.NfcMessage;
import okhttp3.WebSocket;

public class RequestResponseSynchronizer {

    private final WebSocket webSocket;
    
    private Map<Integer, RequestResponse> map = new ConcurrentHashMap<>();

    public RequestResponseSynchronizer(WebSocket webSocket) {
        this.webSocket = webSocket;
    }

    public <T extends NfcMessage> transmit(NfcMessage message, long timeout) {
        
        
        
    }

}
