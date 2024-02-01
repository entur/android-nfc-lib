package no.entur.android.nfc.websocket.server;

import org.junit.jupiter.api.Test;

import no.entur.android.nfc.websocket.client.WebSocketClient;
import no.entur.android.nfc.websocket.client.WebSocketClientFactory;

public class ClientTest {

    @Test
    public void testConnect() throws Exception {

        int port = 8891; // 843 flash policy port
        WebSocketNfcServer s = new WebSocketNfcServer(port);
        s.start();
        try {
            System.out.println("Begin");
            WebSocketClientFactory factory = new WebSocketClientFactory();
            WebSocketClient connect = factory.connect("ws://127.0.0.1:" + port, null);
            try {
                System.out.println("Connect");

                connect.getReaderClient().connect();

                Thread.sleep(500);

                System.out.println("Connected");
            } finally {
                connect.close();
            }
        } finally {
            s.stop();
        }
    }

}
