package no.entur.android.nfc.external.minova.service;

import java.io.IOException;
import java.io.InputStreamReader;

import no.entur.android.nfc.tcpserver.CommandInput;

public class CommaCommandInput implements CommandInput<String> {

    private final InputStreamReader reader;

    public CommaCommandInput(InputStreamReader reader) {
        this.reader = reader;
    }

    @Override
    public String read() throws IOException {

        StringBuilder builder = new StringBuilder(128);

        do {
            int read = reader.read();
            if(read == -1) {
                return null;
            } else if(read == ',') {
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
