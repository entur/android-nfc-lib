package no.entur.android.nfc.tcpserver;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class CharCommandOutput implements CommandOutput<String> {

    private final OutputStreamWriter writer;
    private final char terminatorCharacter;

    public CharCommandOutput(char terminatorCharacter, OutputStreamWriter writer) {
        this.terminatorCharacter = terminatorCharacter;
        this.writer = writer;
    }

    @Override
    public void write(String command) throws IOException {
        writer.write(command);
        if(command.charAt(command.length() - 1) != terminatorCharacter) {
            writer.write(terminatorCharacter);
        }
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
