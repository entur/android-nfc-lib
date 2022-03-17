package no.entur.android.nfc.tcpserver;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class CommaCommandOutput implements CommandOutput<String> {

    private final OutputStreamWriter writer;

    public CommaCommandOutput(OutputStreamWriter writer) {
        this.writer = writer;
    }

    @Override
    public void write(String command) throws IOException {
        writer.write(command);
        writer.write(',');
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
