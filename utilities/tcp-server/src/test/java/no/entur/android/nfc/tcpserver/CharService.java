package no.entur.android.nfc.tcpserver;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CharService implements CommandServer.Listener, CommandInputOutputThread.Listener<String, String> {

    private CommandServer server;
    private char terminatorCharacter;
    private List<CommandInputOutputThread<String, String>> clients = new ArrayList<>();

    private List<String> commandHistory = new ArrayList<>(); // for testing

    public CharService(char terminatorCharacter, int port) {
        this.terminatorCharacter = terminatorCharacter;
        this.server = new CommandServer(this, port);
    }

    @Override
    public void onServerSocketStart(int port) {
        System.out.println(Thread.currentThread().getName() + ": On server start " + port);
    }

    @Override
    public void onServerSocketConnection(int port, Socket socket) throws IOException {
        CommandInput<String> input = new CharCommandInput(terminatorCharacter, new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        CommandOutput<String> output = new CharCommandOutput(terminatorCharacter, new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        CommandInputOutputThread thread = new CommandInputOutputThread(this, socket, output, input);

        synchronized (clients) {
            clients.add(thread);
        }

        thread.start();
    }

    @Override
    public void onServerSocketClosed(int port, Exception e) {
        System.out.println(Thread.currentThread().getName() + ": On server closed " + port + ": " + e);
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
        System.out.println(Thread.currentThread().getName() + ": On reader connect " + reader);
    }

    @Override
    public void onReaderCommand(CommandInputOutputThread<String, String> reader, String input)  {
        System.out.println(Thread.currentThread().getName() + ": On reader command " + reader + " " + input);

        try {
            String s = reader.outputInput("MCR04G, GETTYPE");
            commandHistory.add(s);
            System.out.println(Thread.currentThread().getName() + ": Tag type is " + s);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onReaderClosed(CommandInputOutputThread<String, String> reader, Exception e) {
        System.out.println(Thread.currentThread().getName() + ": On reader disconnected " + reader + ": " + e);
        synchronized (clients) {
            clients.remove(reader);
        }
    }

    public void start()  {
        server.start();
    }

    public void stop() throws IOException {
        server.stop();
    }

    public List<String> getCommandHistory() {
        return commandHistory;
    }
}
