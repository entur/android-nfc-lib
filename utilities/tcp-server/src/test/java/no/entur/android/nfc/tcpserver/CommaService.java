package no.entur.android.nfc.tcpserver;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CommaService implements CommandServer.Listener, CommandInputOutputThread.Listener<String, String> {

    private CommandServer server;
    private List<CommandInputOutputThread<String, String>> clients = new ArrayList<>();

    public CommaService(int port) {
        server = new CommandServer(this, port);
    }

    @Override
    public void onServerSocketStart(int port) {
        System.out.println("On server start " + port);
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
    public void onReaderStart(CommandInputOutputThread<String, String> reader) {
        System.out.println("On reader connect " + reader);
    }

    @Override
    public void onReaderCommand(CommandInputOutputThread<String, String> reader, String input)  {
        System.out.println("On reader command " + reader + " " + input);

        try {
            reader.write("TestResponse");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReaderClosed(CommandInputOutputThread<String, String> reader, Exception e) {
        System.out.println("On reader disconnected " + reader+ ": " + e);
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

}
