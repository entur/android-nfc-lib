package no.entur.android.nfc.external.minova.service;

import java.io.IOException;
import java.io.OutputStreamWriter;

import no.entur.android.nfc.tcpserver.CommandOutput;

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
