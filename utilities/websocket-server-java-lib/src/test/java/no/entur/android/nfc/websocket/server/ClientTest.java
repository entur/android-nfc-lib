package no.entur.android.nfc.websocket.server;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import no.entur.android.nfc.websocket.client.WebSocketClient;
import no.entur.android.nfc.websocket.client.WebSocketClientFactory;

public class ClientTest {

    @Test
    @Disabled
    public void testConnect() throws Exception {
        ExtendedCardTerminalFactory extendedCardTerminalFactory = new ExtendedCardTerminalFactory(new DefaultCardTerminalMetadataEnricher());

        int port = 8199; // 843 flash policy port
        WebSocketNfcServer s = new WebSocketNfcServer(port, new NoopCardTerminalsFilter(), extendedCardTerminalFactory);
        s.start();
        try {
            System.out.println("Begin");
            WebSocketClientFactory factory = new WebSocketClientFactory(12000, 1000);
            WebSocketClient connect = factory.connect("ws://127.0.0.1:" + port, null);
            try {
                System.out.println("Connect");

                connect.getReaderClient().connect(new String[]{});

                Thread.sleep(50000);

                System.out.println("Connected");
            } finally {
                connect.close();
            }
        } finally {
            s.stop();
        }
    }

}
