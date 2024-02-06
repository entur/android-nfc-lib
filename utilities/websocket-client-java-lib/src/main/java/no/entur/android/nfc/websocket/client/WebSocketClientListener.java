package no.entur.android.nfc.websocket.client;

/**
 *
 * Interface which allows for cleanup once a client is closed
 *
 */

public interface WebSocketClientListener {

    void onClosed();

}
