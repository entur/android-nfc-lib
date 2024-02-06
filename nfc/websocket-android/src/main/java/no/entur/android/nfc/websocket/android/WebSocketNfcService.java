package no.entur.android.nfc.websocket.android;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import no.entur.android.nfc.external.service.tag.INFcTagBinder;
import no.entur.android.nfc.websocket.client.WebSocketClient;
import no.entur.android.nfc.websocket.client.WebSocketClientFactory;
import no.entur.android.nfc.websocket.client.WebSocketClientListener;
import no.entur.android.nfc.websocket.messages.CompositeNfcMessageListener;
import no.entur.android.nfc.websocket.messages.NfcMessageListener;
import no.entur.android.nfc.websocket.messages.RequestResponseMessages;
import no.entur.android.nfc.websocket.messages.card.CardClient;
import no.entur.android.nfc.websocket.messages.reader.ReaderClient;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;

public class WebSocketNfcService extends Service implements CardClient.Listener, WebSocketClientListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketNfcService.class);

    public static final String ANDROID_PERMISSION_NFC = "android.permission.NFC";

    protected WebsocketTagProxyStore store = new WebsocketTagProxyStore();
    protected INFcTagBinder infcTagBinder;

    //binder given to client
    private final IBinder binder = new LocalBinder();

    private WebSocketClientFactory factory = new WebSocketClientFactory();

    private WebSocketClient client = null;

    public class LocalBinder extends Binder {
        public boolean connect(String uri) {
            return WebSocketNfcService.this.connect(uri);
        }

        public boolean connectReader() {
            return WebSocketNfcService.this.connectReader();
        }

        public boolean disconnectReader() {
            return WebSocketNfcService.this.disconnectReader();
        }

        public void disconnect() throws IOException {
            WebSocketNfcService.this.disconnect();
        }


    }

    @Override
    public void onCreate() {
        super.onCreate();

        LOGGER.info("onCreate");

        this.infcTagBinder = new INFcTagBinder(store);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public boolean connect(String uri) {
        try {
            LOGGER.warn("Connect to " + uri);
            client = factory.connect(uri, this);
        } catch(Exception e) {
            LOGGER.warn("Problem connecting to " + uri);
            return false;
        }
        return true;
    }

    public boolean connectReader() {
        WebSocketClient c = this.client;
        if(c != null) {
            return c.getReaderClient().connect();
        }
        return false;
    }

    public boolean disconnectReader() {
        WebSocketClient c = this.client;
        if(c != null) {
            return c.getReaderClient().disconnect();
        }
        return false;
    }

    public void disconnect() throws IOException {
        WebSocketClient c = this.client;
        if(c != null) {
            c.close();
        }

    }

    @Override
    public void onClosed() {
        this.client = null;
    }

    @Override
    public void onCardLost() {
        LOGGER.info("onCardLost");
    }

    @Override
    public void onCardPresent(List<String> technologies) {
        LOGGER.info("onCardPresent: " + technologies);
    }

    public void broadcast(String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        sendBroadcast(intent, ANDROID_PERMISSION_NFC);
    }

}
