package no.entur.android.nfc.tcpserver;

import org.junit.jupiter.api.Test;

import java.io.IOException;

public class CommandServerTest {

    @Test
    public void testListener() throws IOException, InterruptedException {

        CommaService service = new CommaService(8080);
        try {
            service.start();

            CommaClient client = new CommaClient("127.0.0.1", 8080);

            Thread.sleep(100);

            client.connect();

            client.write("TestCommand");
            String read = client.read();
            System.out.println("Server responded with " + read);
            System.out.println(read);
            Thread.sleep(100);
        } finally {
            service.stop();
        }
        Thread.sleep(100);

    }

}
