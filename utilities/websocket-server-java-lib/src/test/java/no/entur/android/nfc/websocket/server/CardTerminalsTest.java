package no.entur.android.nfc.websocket.server;

import org.junit.jupiter.api.Test;

import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;

public class CardTerminalsTest {

    @Test
    public void test() throws InterruptedException {
        TerminalFactory f = TerminalFactory.getDefault();
        CardTerminals terminals = f.terminals();

        CardTerminalsPollingServer cardTerminalsPollingServer = new CardTerminalsPollingServer(f, new CardTerminalsPollingPool());
        cardTerminalsPollingServer.start();
        Thread.sleep(1000);
        cardTerminalsPollingServer.stop();

    }
}
