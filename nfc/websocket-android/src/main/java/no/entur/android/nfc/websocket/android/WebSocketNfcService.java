package no.entur.android.nfc.websocket.android;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import no.entur.android.nfc.external.service.tag.INFcTagBinder;
import no.entur.android.nfc.websocket.messages.CompositeNfcMessageListener;
import no.entur.android.nfc.websocket.messages.NfcMessageListener;
import no.entur.android.nfc.websocket.messages.RequestResponseMessages;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;

public class WebSocketNfcService extends Service {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketNfcService.class);

    public static final String ANDROID_PERMISSION_NFC = "android.permission.NFC";

    private class DefaultWebSocketReader extends WebSocketNfcMessageReader {

        private OkHttpClient client;

        public DefaultWebSocketReader(NfcMessageListener delegate, OkHttpClient client) {
            super(delegate);
            this.client = client;
        }

        @Override
        public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            super.onClosed(webSocket, code, reason);

            // Trigger shutdown of the dispatcher's executor so this process can exit cleanly.
            client.dispatcher().executorService().shutdown();
        }
    }

    protected WebsocketTagProxyStore store = new WebsocketTagProxyStore();
    protected INFcTagBinder infcTagBinder;

    //binder given to client
    private final IBinder binder = new LocalBinder();
    private OkHttpClient client;
    private WebSocket webSocket;

    public class LocalBinder extends Binder {
        public WebSocketNfcService getService() {
            return WebSocketNfcService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        this.infcTagBinder = new INFcTagBinder(store);
    }

    public void initialize(String url) {
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(0,  TimeUnit.MILLISECONDS)
                .build();

        CompositeNfcMessageListener local = new CompositeNfcMessageListener();

        WebsocketNfcMessageWriter sender = new WebsocketNfcMessageWriter();

        Request request = new Request.Builder().url(url).build();

        RequestResponseMessages requestResponseMessages = new RequestResponseMessages(local, sender);

        WebSocketNfcMessageReader reader = new WebSocketNfcMessageReader()

        WebSocket webSocket = client.newWebSocket(request, new WebsocketNfcMessageWriter());

    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void broadcast(String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        sendBroadcast(intent, ANDROID_PERMISSION_NFC);
    }

    public void connectService(String url) {

        client = new OkHttpClient.Builder()
                .readTimeout(0,  TimeUnit.MILLISECONDS)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .build();
        webSocket = client.newWebSocket(request, this);
    }

    public void disconnectService() {

    }

    public void connectReader() {

    }

    public void disconnect() {

    }



}
