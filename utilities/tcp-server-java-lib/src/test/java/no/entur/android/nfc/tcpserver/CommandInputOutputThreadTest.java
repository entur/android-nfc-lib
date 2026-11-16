package no.entur.android.nfc.tcpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class CommandInputOutputThreadTest {

    private static final String TERMINATOR = "\r\n";

    private static class RecordingListener implements CommandInputOutputThread.Listener<String, String> {

        private final BlockingQueue<String> commands = new LinkedBlockingQueue<>();
        private final CompletableFuture<Exception> closed = new CompletableFuture<>();

        @Override
        public void onReaderStart(CommandInputOutputThread<String, String> reader) {
        }

        @Override
        public void onReaderCommand(CommandInputOutputThread<String, String> reader, String input) {
            commands.add(input);
        }

        @Override
        public void onReaderClosed(CommandInputOutputThread<String, String> reader, Exception e) {
            closed.complete(e);
        }
    }

    // treat EVENT messages as unsolicited
    private static class EventCommandInputOutputThread extends CommandInputOutputThread<String, String> {

        public EventCommandInputOutputThread(Listener<String, String> listener, Socket clientSocket, CommandOutput<String> out, CommandInput<String> in) {
            super(listener, clientSocket, out, in);
        }

        @Override
        protected boolean isResponse(String input) {
            return !input.startsWith("EVENT");
        }
    }

    private ServerSocket serverSocket;
    private TerminatorClient client;
    private Socket serverSide;
    private RecordingListener listener;
    private CommandInputOutputThread<String, String> thread;

    @BeforeEach
    public void before() throws IOException {
        serverSocket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
        client = new TerminatorClient(TERMINATOR, "127.0.0.1", serverSocket.getLocalPort());
        client.connect();
        serverSide = serverSocket.accept();
        listener = new RecordingListener();
    }

    @AfterEach
    public void after() throws Exception {
        if(thread != null) {
            thread.close();
            thread.join(5000);
        }
        client.close();
        serverSocket.close();
    }

    private void start(boolean events) throws IOException {
        CommandInput<String> input = new TerminatorCommandInput(TERMINATOR, new InputStreamReader(serverSide.getInputStream(), StandardCharsets.UTF_8));
        CommandOutput<String> output = new TerminatorCommandOutput(TERMINATOR, new OutputStreamWriter(serverSide.getOutputStream(), StandardCharsets.UTF_8));
        if(events) {
            thread = new EventCommandInputOutputThread(listener, serverSide, output, input);
        } else {
            thread = new CommandInputOutputThread<>(listener, serverSide, output, input);
        }
        thread.start();
    }

    @Test
    public void testOutputInput() throws Exception {
        start(false);

        CompletableFuture<String> response = CompletableFuture.supplyAsync(() -> {
            try {
                return thread.outputInput("COMMAND");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        assertEquals("COMMAND", client.read());
        client.write("RESPONSE");

        assertEquals("RESPONSE", response.get(5, TimeUnit.SECONDS));
    }

    @Test
    public void testTimeout() throws Exception {
        start(false);

        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            assertThrows(SocketTimeoutException.class, () -> thread.outputInput("COMMAND", 100));
        });

        // a late response is passed to the listener, and not to the next command
        assertEquals("COMMAND", client.read());
        client.write("LATE");
        assertEquals("LATE", listener.commands.poll(5, TimeUnit.SECONDS));

        CompletableFuture<String> response = CompletableFuture.supplyAsync(() -> {
            try {
                return thread.outputInput("NEXT");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        assertEquals("NEXT", client.read());
        client.write("NEXT RESPONSE");
        assertEquals("NEXT RESPONSE", response.get(5, TimeUnit.SECONDS));
    }

    @Test
    public void testDisconnectWhileWaitingForResponse() throws Exception {
        start(false);

        CompletableFuture<String> response = CompletableFuture.supplyAsync(() -> {
            try {
                return thread.outputInput("COMMAND", 60000);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        assertEquals("COMMAND", client.read());
        client.close();

        ExecutionException e = assertThrows(ExecutionException.class, () -> response.get(5, TimeUnit.SECONDS));
        assertTrue(e.getCause().getCause() instanceof EOFException, e.toString());

        // closed due to failure (end of stream)
        assertNotNull(listener.closed.get(5, TimeUnit.SECONDS));
        assertTrue(thread.isClosed());

        // further commands fail immediately
        assertThrows(EOFException.class, () -> thread.outputInput("COMMAND", 60000));
    }

    @Test
    public void testCloseWhileWaitingForResponse() throws Exception {
        start(false);

        CompletableFuture<String> response = CompletableFuture.supplyAsync(() -> {
            try {
                return thread.outputInput("COMMAND", 60000);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        assertEquals("COMMAND", client.read());
        thread.close();

        ExecutionException e = assertThrows(ExecutionException.class, () -> response.get(5, TimeUnit.SECONDS));
        assertTrue(e.getCause().getCause() instanceof EOFException, e.toString());

        // closed on purpose, so no exception
        assertNull(listener.closed.get(5, TimeUnit.SECONDS));
    }

    @Test
    public void testUnsolicitedInputWhileWaitingForResponse() throws Exception {
        start(true);

        CompletableFuture<String> response = CompletableFuture.supplyAsync(() -> {
            try {
                return thread.outputInput("COMMAND");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        assertEquals("COMMAND", client.read());
        client.write("EVENT 1");
        client.write("RESPONSE");

        assertEquals("RESPONSE", response.get(5, TimeUnit.SECONDS));
        assertEquals("EVENT 1", listener.commands.poll(5, TimeUnit.SECONDS));
    }

    @Test
    public void testReaderIdFromFirstMessageWithComma() throws Exception {
        start(false);

        client.write("NO COMMA");
        assertEquals("NO COMMA", listener.commands.poll(5, TimeUnit.SECONDS));
        assertNull(thread.getReaderId());

        client.write("MCR04G-50F2,UID=807644D24E2904");
        assertEquals("MCR04G-50F2,UID=807644D24E2904", listener.commands.poll(5, TimeUnit.SECONDS));
        assertEquals("MCR04G-50F2", thread.getReaderId());
    }
}
