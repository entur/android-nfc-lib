package no.entur.android.nfc.external.minova;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import no.entur.android.nfc.external.minova.reader.McrCommandSetBuilder;
import no.entur.android.nfc.external.minova.reader.MinovaCommandInputOutputThread;
import no.entur.android.nfc.external.minova.reader.MinovaCommands;
import no.entur.android.nfc.tcpserver.CommandInputOutputThread;
import no.entur.android.nfc.tcpserver.TerminatorCommandInput;
import no.entur.android.nfc.tcpserver.TerminatorCommandOutput;

public class MinovaCommandInputOutputThreadTest {

    @Test
    public void parseUid() {
        assertEquals("807644D24E2904", MinovaCommandInputOutputThread.parseUid("MCR04G-50F2,UID=807644D24E2904"));
        assertEquals("1255CCF0", MinovaCommandInputOutputThread.parseUid("MCR02-8AC64C,UID=1255CCF0,IO=0F"));
        assertEquals("803BD2E26E2C04", MinovaCommandInputOutputThread.parseUid("MCR04G, UID=803BD2E26E2C04"));

        assertNull(MinovaCommandInputOutputThread.parseUid("MCR04G-50F2,CARDTYPE=0344;20;067577810280"));
        assertNull(MinovaCommandInputOutputThread.parseUid("MCR04G-50F2,RAPDU=9000"));
        assertNull(MinovaCommandInputOutputThread.parseUid("MCR04G-50F2,UID="));
        assertNull(MinovaCommandInputOutputThread.parseUid("MCR04G-50F2"));
    }

    @Test
    public void newCardWhileWaitingForResponse() throws Exception {
        BlockingQueue<String> commands = new LinkedBlockingQueue<>();

        CommandInputOutputThread.Listener<String, String> listener = new CommandInputOutputThread.Listener<String, String>() {
            @Override
            public void onReaderStart(CommandInputOutputThread<String, String> reader) {
            }

            @Override
            public void onReaderCommand(CommandInputOutputThread<String, String> reader, String input) {
                commands.add(input);
            }

            @Override
            public void onReaderClosed(CommandInputOutputThread<String, String> reader, Exception e) {
            }
        };

        String terminator = McrCommandSetBuilder.COMMAND_SET_SEPERATOR;
        try (ServerSocket serverSocket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
             Socket client = new Socket(InetAddress.getLoopbackAddress(), serverSocket.getLocalPort());
             Socket serverSide = serverSocket.accept()) {

            client.setSoTimeout(5000);

            TerminatorCommandInput clientInput = new TerminatorCommandInput(terminator, new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
            TerminatorCommandOutput clientOutput = new TerminatorCommandOutput(terminator, new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8));

            MinovaCommandInputOutputThread thread = new MinovaCommandInputOutputThread(listener, serverSide,
                    new TerminatorCommandOutput(terminator, new OutputStreamWriter(serverSide.getOutputStream(), StandardCharsets.UTF_8)),
                    new TerminatorCommandInput(terminator, new InputStreamReader(serverSide.getInputStream(), StandardCharsets.UTF_8)));
            thread.start();
            try {
                clientOutput.write("MCR04G-50F2,UID=807644D24E2904");
                assertEquals("MCR04G-50F2,UID=807644D24E2904", commands.poll(5, TimeUnit.SECONDS));

                MinovaCommands minovaCommands = new MinovaCommands(thread);

                CompletableFuture<String> type = CompletableFuture.supplyAsync(() -> {
                    try {
                        return minovaCommands.getType();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

                assertEquals("MCR04G-50F2,GETTYPE,", clientInput.read());

                // new card before the response
                clientOutput.write("MCR04G-50F2,UID=16FE1E24");
                clientOutput.write("MCR04G-50F2,CARDTYPE=0344;20;067577810280");

                assertEquals("0344;20;067577810280", type.get(5, TimeUnit.SECONDS));
                assertEquals("MCR04G-50F2,UID=16FE1E24", commands.poll(5, TimeUnit.SECONDS));
            } finally {
                thread.close();
                thread.join(5000);
            }
        }
    }
}
