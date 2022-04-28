package no.entur.android.nfc.tcpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class CommandServerTest {

    @Test
    public void testListener() throws IOException, InterruptedException {

        CharService service = new CharService('\n', 8080);
        try {
            service.start();

            CharClient externalNfcReader = new CharClient('\n', "127.0.0.1", 8080);

            Thread.sleep(100);

            externalNfcReader.connect();

            // write without expecting anything in return
            externalNfcReader.write("MCR04G, UID=803BD2E26E2C04");

            String read = externalNfcReader.read();
            System.out.println(Thread.currentThread().getName() + ": Server asks " + read);
            Thread.sleep(100);
            externalNfcReader.write("CARDTYPE=0344;20;067577810280");
            Thread.sleep(100);
            List<String> commandHistory = service.getCommandHistory();
            assertEquals(commandHistory.get(0), "CARDTYPE=0344;20;067577810280");
        } finally {
            service.stop();
        }
        Thread.sleep(100);

    }

}
