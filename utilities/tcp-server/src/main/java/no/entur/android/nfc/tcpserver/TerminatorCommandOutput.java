package no.entur.android.nfc.tcpserver;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class TerminatorCommandOutput implements CommandOutput<String> {

    private final OutputStreamWriter writer;
    private final char[] terminator;

    public TerminatorCommandOutput(String terminator, OutputStreamWriter writer) {
        this.terminator = terminator.toCharArray();
        this.writer = writer;
    }

    @Override
    public void write(String command) throws IOException {
        writer.write(command);
        writer.write(terminator);
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
