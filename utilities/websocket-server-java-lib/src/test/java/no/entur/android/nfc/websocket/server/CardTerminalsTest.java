package no.entur.android.nfc.websocket.server;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;

public class CardTerminalsTest {

    @Test
    @Disabled
    public void test() throws InterruptedException {
        TerminalFactory f = TerminalFactory.getDefault();

        ExtendedCardTerminalFactory extendedCardTerminalFactory = new ExtendedCardTerminalFactory(new DefaultCardTerminalMetadataEnricher());


        CardTerminalsPollingServer cardTerminalsPollingServer = new CardTerminalsPollingServer(f, new CardTerminalsPollingPool(extendedCardTerminalFactory));
        cardTerminalsPollingServer.start();
        Thread.sleep(1000);
        cardTerminalsPollingServer.stop();

    }
}
