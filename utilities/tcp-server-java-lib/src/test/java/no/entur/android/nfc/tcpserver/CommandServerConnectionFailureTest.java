package no.entur.android.nfc.tcpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CommandServerConnectionFailureTest {

    @Test
    public void testServerSurvivesFailingConnection() throws Exception {
        int port;
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }

        AtomicInteger count = new AtomicInteger();
        BlockingQueue<Socket> accepted = new LinkedBlockingQueue<>();

        CommandServer server = new CommandServer(new CommandServer.Listener() {
            @Override
            public void onServerSocketStart(int port) {
            }

            @Override
            public void onServerSocketConnection(int port, Socket socket) throws IOException {
                if(count.incrementAndGet() == 1) {
                    throw new IOException("First connection fails");
                }
                accepted.add(socket);
            }

            @Override
            public void onServerSocketClosed(int port, Exception e) {
            }
        }, port);

        server.start();
        try {
            Socket first = connect(port);
            // the failing connection is closed by the server
            assertEquals(-1, first.getInputStream().read());
            first.close();

            Socket second = connect(port);
            Socket serverSide = accepted.poll(5, TimeUnit.SECONDS);
            assertEquals(2, count.get());
            serverSide.close();
            second.close();
        } finally {
            server.stop();
        }
    }

    private static Socket connect(int port) throws Exception {
        long deadline = System.currentTimeMillis() + 5000;
        while(true) {
            try {
                Socket socket = new Socket("127.0.0.1", port);
                socket.setSoTimeout(5000);
                return socket;
            } catch (IOException e) {
                // server not started yet
                if(System.currentTimeMillis() > deadline) {
                    throw e;
                }
                Thread.sleep(10);
            }
        }
    }
}
