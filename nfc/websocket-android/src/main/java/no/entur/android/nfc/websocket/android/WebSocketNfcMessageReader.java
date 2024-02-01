package no.entur.android.nfc.websocket.android;

import android.util.Log;

import no.entur.android.nfc.websocket.messages.NfcMessage;
import no.entur.android.nfc.websocket.messages.NfcMessageListener;
import no.entur.android.nfc.websocket.messages.NfcMessageReader;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketNfcMessageReader extends WebSocketListener {

    private static final String LOG_TAG = WebSocketNfcMessageReader.class.getName();

    private final NfcMessageListener delegate;

    private final NfcMessageReader reader = new NfcMessageReader();

    public WebSocketNfcMessageReader(NfcMessageListener delegate) {
        this.delegate = delegate;
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        Log.i(LOG_TAG, "onOpen");
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        Log.i(LOG_TAG, "onMessage: " + text);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        Log.i(LOG_TAG, "onMessage");

        NfcMessage m = reader.parse(bytes.toByteArray());
        if(m != null) {
            delegate.onMessage(m);
        }

    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        Log.i(LOG_TAG, "onClosing");
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        Log.i(LOG_TAG, "onFailure", t);
    }
}
