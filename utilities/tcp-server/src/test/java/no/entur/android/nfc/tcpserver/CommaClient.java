package no.entur.android.nfc.tcpserver;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class CommaClient implements Closeable {

    private final String host;
    private final int port;

    private Socket socket;
    private CommaCommandInput in;
    private CommaCommandOutput out;

    private boolean closed = false;

    public CommaClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() throws IOException {
        socket = new Socket(host, port);

        in = new CommaCommandInput(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        out = new CommaCommandOutput(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
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
