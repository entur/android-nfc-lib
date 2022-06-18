package no.entur.android.nfc.tcpserver;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;

public class CharCommandInput implements CommandInput<String> {

    private final BufferedReader reader;
    private final char terminatorCharacter;

    private StringBuilder builder = new StringBuilder(128);

    public CharCommandInput(char terminatorCharacter, InputStreamReader reader) {
        this.terminatorCharacter = terminatorCharacter;
        this.reader = new BufferedReader(reader);
    }

    @Override
    public String read() throws IOException {
        try {
            do {
                int read = reader.read();
                if (read == -1) {
                    if (builder.length() == 0) {
                        throw new EOFException();
                    }
                    break;
                } else if (read == terminatorCharacter) {
                    break;
                }
                builder.append((char) read);
            } while (true);

            return builder.toString();
        } finally {
            builder.setLength(0);
        }
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
