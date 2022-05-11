package no.entur.android.nfc.tcpserver;

import java.io.IOException;
import java.io.InputStreamReader;

public class CharCommandInput implements CommandInput<String> {

    private final InputStreamReader reader;
    private final char terminatorCharacter;

    public CharCommandInput(char terminatorCharacter, InputStreamReader reader) {
        this.terminatorCharacter = terminatorCharacter;
        this.reader = reader;
    }

    @Override
    public String read() throws IOException {

        StringBuilder builder = new StringBuilder(128);

        do {
            int read = reader.read();
            if(read == -1) {
                return null;
            } else if(read == terminatorCharacter) {
                break;
            }
            builder.append((char)read);
        } while(true);

        return builder.toString();
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
