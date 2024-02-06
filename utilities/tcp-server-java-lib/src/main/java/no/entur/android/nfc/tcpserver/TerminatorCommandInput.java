package no.entur.android.nfc.tcpserver;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class TerminatorCommandInput implements CommandInput<String> {

    private final BufferedReader reader;
    private final char[] terminator;

    private StringBuilder builder = new StringBuilder(128);

    public TerminatorCommandInput(String terminator, Reader reader) {
        this.terminator = terminator.toCharArray();
        this.reader = new BufferedReader(reader);
    }

    @Override
    public String read() throws IOException {
        try {
            do {
                int read = reader.read();
                if (read == -1) {
                    // exit now even if there is data in the buffer
                    throw new EOFException();
                }

                char readChar = (char) read;
                builder.append(readChar);

                if(readChar == terminator[terminator.length - 1]) {
                    // check if full terminator has been read

                    if(endsWithTerminator()) {
                        break;
                    }
                }

            } while (true);

            builder.setLength(builder.length() - terminator.length);

            return builder.toString();
        } finally {
            builder.setLength(0);
        }
    }

    private boolean endsWithTerminator() {
        if(builder.length() < terminator.length) {
            return false;
        }
        for(int i = 0; i < terminator.length; i++) {
            if(builder.charAt(builder.length() - terminator.length + i) != terminator[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
