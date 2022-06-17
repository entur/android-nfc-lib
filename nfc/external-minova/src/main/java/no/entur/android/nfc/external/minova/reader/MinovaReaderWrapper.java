package no.entur.android.nfc.external.minova.reader;

import android.content.Context;
import android.content.Intent;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import no.entur.android.nfc.external.ExternalNfcReaderCallback;
import no.entur.android.nfc.external.minova.service.CommaCommandInput;
import no.entur.android.nfc.external.minova.service.CommaCommandOutput;
import no.entur.android.nfc.tcpserver.CommandInput;
import no.entur.android.nfc.tcpserver.CommandInputOutputThread;
import no.entur.android.nfc.tcpserver.CommandOutput;
import no.entur.android.nfc.tcpserver.CommandServer;

public class MinovaReaderWrapper implements CommandServer.Listener, CommandInputOutputThread.Listener<String, String> {

    public final List<CommandInputOutputThread<String, String>> clients = new ArrayList<>();

    private final CommandServer server;
    private final ReaderListener listener;
    private final Context context;

    public MinovaReaderWrapper(ReaderListener listener, int port, Context context) {
        this.server = new CommandServer(this, port);
        this.listener = listener;
        this.context = context;
    }

    public void start() {
        server.start();
    }

    public String sendCommandForResponse(int slot, String command) {
        String result = null;
        try {
            result = clients.get(slot).outputInput(clients.get(slot).getReaderId() + ", " + command);
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

        CommandInputOutputThread<String, String> thread = new CommandInputOutputThread<>(this, socket, output, input);

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
    public void onReaderStart(CommandInputOutputThread<String, String> reader) {
        System.out.println("Reader connected!");

        if (reader != null) {
            System.out.println("reader is not null ^^");
            MinovaCommands commands = new MinovaCommands(reader);
            IMcr0XBinder mcrBinder = new IMcr0XBinder();
            mcrBinder.setCommands(commands);

            McrReader mcrReader = new McrReader("MCR04G ", mcrBinder);
            System.out.println("Made McrReader");

            Intent intent = new Intent();
            intent.setAction(ExternalNfcReaderCallback.ACTION_READER_OPENED);
            intent.putExtra(ExternalNfcReaderCallback.EXTRA_READER_CONTROL, mcrReader);

            //intent.putExtra(ExternalNfcReaderCallback.EXTRA_READER_STATUS_CODE, status);

            context.sendBroadcast(intent);
        }
    }

    @Override
    public void onReaderCommand(CommandInputOutputThread<String, String> reader, String input) {
        System.out.println("On reader command " + reader + " " + input);

        if (input.contains("UID")) {
            try {
                listener.onTagPresent(clients.indexOf(reader), input.substring(input.lastIndexOf("=") + 1));
            } catch (Exception e) {
                System.out.print(e.getLocalizedMessage());
            }
        }
    }

    @Override
    public void onReaderClosed(CommandInputOutputThread<String, String> reader, Exception e) {

    }

    public interface ReaderListener {
        void onTagPresent(int slot, String uid);
    }

}
