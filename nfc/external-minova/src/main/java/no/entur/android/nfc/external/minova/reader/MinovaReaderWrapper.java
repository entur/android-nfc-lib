package no.entur.android.nfc.external.minova.reader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import no.entur.android.nfc.external.minova.service.CommaCommandInput;
import no.entur.android.nfc.external.minova.service.CommaCommandOutput;
import no.entur.android.nfc.tcpserver.CommandInput;
import no.entur.android.nfc.tcpserver.CommandInputOutputThread;
import no.entur.android.nfc.tcpserver.CommandOutput;
import no.entur.android.nfc.tcpserver.CommandServer;

public class MinovaReaderWrapper implements CommandServer.Listener, CommandInputOutputThread.Listener<String, String>  {

    public List<CommandInputOutputThread<String, String>> clients = new ArrayList<>();

    private CommandServer server;
    private ReaderListener listener;

    public MinovaReaderWrapper(ReaderListener listener, int port) {
        this.listener = listener;
        server = new CommandServer(this, port);
    }

    public void transmit(byte[] data) {

    }

    public void start() {
        server.start();
    }

    public String sendCommandForResponse(int slot, String command) {
        String result = null;
        try {
            result = clients.get(slot).outputInput("MCR04G-8E71, " + command);
        } catch (Exception e) {
            System.out.println("Oh no!");
        }
        return result;
    }

    @Override
    public void onServerSocketStart(int port) {
        System.out.println("Server started!");
    }

    @Override
    public void onServerSocketConnection(int port, Socket socket) throws IOException {
        CommandInput<String> input = new CommaCommandInput(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        CommandOutput<String> output = new CommaCommandOutput(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

        CommandInputOutputThread thread = new CommandInputOutputThread(this, socket, output, input);

        synchronized (clients) {
            clients.add(thread);
        }

        thread.start();
    }

    @Override
    public void onServerSocketClosed(int port, Exception e) {
        System.out.println("On server closed " + port + ": " + e);
        synchronized (clients) {
            for (CommandInputOutputThread<String, String> client : clients) {
                client.close();
                try {
                    client.join();
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
            clients.clear();
        }
    }

    @Override
    public void onReaderStart(CommandInputOutputThread reader) {
        System.out.println("Reader connected!");

    }

    @Override
    public void onReaderCommand(CommandInputOutputThread reader, String input) {
        System.out.println("On reader command " + reader + " " + input);

        if (input.contains("UID")) {
            try {
                listener.onTagPresent(clients.indexOf(reader) ,input.substring(input.lastIndexOf("=") + 1));
            } catch (Exception e) {
                System.out.print(e.getLocalizedMessage());
            }
        }
    }

    @Override
    public void onReaderClosed(CommandInputOutputThread reader, Exception e) {

    }

    public interface ReaderListener {
        void onTagPresent(int slot, String uid);
    }

}
