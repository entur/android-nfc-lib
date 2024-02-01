package no.entur.android.nfc.websocket.client;

import no.entur.android.nfc.websocket.messages.NfcMessageListener;
import okhttp3.OkHttpClient;
import okhttp3.WebSocket;

public class DefaultWebSocketReader extends WebSocketNfcMessageReader {

        private OkHttpClient client;
        private WebSocketClientListener listener;

        public DefaultWebSocketReader(NfcMessageListener delegate, OkHttpClient client) {
            super(delegate);
            this.client = client;
        }

    public void setListener(WebSocketClientListener listener) {
        this.listener = listener;
    }

    @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            super.onClosed(webSocket, code, reason);

            // Trigger shutdown of the dispatcher's executor so this process can exit cleanly.
            client.dispatcher().executorService().shutdown();

            if(listener != null) {
                listener.onClosed();
            }
        }
    }