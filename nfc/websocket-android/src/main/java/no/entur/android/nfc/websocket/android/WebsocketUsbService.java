package no.entur.android.nfc.websocket.android;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import no.entur.android.nfc.external.service.AbstractService;
import no.entur.android.nfc.external.service.tag.DefaultTagProxyStore;
import no.entur.android.nfc.external.service.tag.INFcTagBinder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebsocketUsbService extends Service {


    private static final Logger LOGGER = LoggerFactory.getLogger(WebsocketUsbService.class);

    public static final String ANDROID_PERMISSION_NFC = "android.permission.NFC";

    private class DefaultWebSocketListener extends WebSocketListener {

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            webSocket.send("Hello...");
            webSocket.send("...World!");
            webSocket.send(ByteString.decodeHex("deadbeef"));
            webSocket.close(1000, "Goodbye, World!");
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            System.out.println("MESSAGE: " + text);
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            System.out.println("MESSAGE: " + bytes.hex());
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(1000, null);
            System.out.println("CLOSE: " + code + " " + reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            t.printStackTrace();
        }
    }

    protected WebsocketTagProxyStore store = new WebsocketTagProxyStore();
    protected INFcTagBinder infcTagBinder;

    //binder given to client
    private final IBinder binder = new LocalBinder();
    private OkHttpClient client;
    private WebSocket webSocket;

    public class LocalBinder extends Binder {
        public WebsocketUsbService getService() {
            return WebsocketUsbService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        this.infcTagBinder = new INFcTagBinder(store);
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
