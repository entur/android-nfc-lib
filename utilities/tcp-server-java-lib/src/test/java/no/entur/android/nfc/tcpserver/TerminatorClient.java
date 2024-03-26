package no.entur.android.nfc.tcpserver;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TerminatorClient implements Closeable {

    private final String host;
    private final int port;

    private final String terminatorCharacters;

    private Socket socket;
    private TerminatorCommandInput in;
    private TerminatorCommandOutput out;

    private boolean closed = false;

    public TerminatorClient(String terminatorCharacters, String host, int port) {
        this.terminatorCharacters = terminatorCharacters;
        this.host = host;
        this.port = port;
    }

    public void connect() throws IOException {
        socket = new Socket(host, port);

        in = new TerminatorCommandInput(terminatorCharacters, new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        out = new TerminatorCommandOutput(terminatorCharacters, new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
    }

    public void write(String command) throws IOException {
        out.write(command);
    }

    public String read() throws IOException {
        return in.read();
    }

    @Override
    public void close()  {
        if(!closed) {
            closed = true;
            try {
                socket.close();
            } catch(Exception e) {
                // ignore
            }
            try {
                in.close();
            } catch(Exception e) {
                // ignore
            }
            try {
                out.close();
            } catch(Exception e) {
                // ignore
            }
        }
    }
}
