package no.entur.android.nfc.tcpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.io.StringReader;

public class TerminatorCommandInputTest {

    @Test
    public void testMultipleLines() {
        String multipleLines = "abcdef\r\nghijkl\r\nmnopqrst\r\n";

        TerminatorCommandInput input = new TerminatorCommandInput("\r\n", new StringReader(multipleLines));

        int count = 0;
        try {
            while(input.read() != null) {
                count++;
            }
        } catch(Exception e) {
            assertEquals(count, 3);
        }
    }

    @Test
    public void testIgnoreIncompleteLine() {
        String multipleLines = "abcdef\r\nghijkl\r\nmnopqr";

        TerminatorCommandInput input = new TerminatorCommandInput("\r\n", new StringReader(multipleLines));

        int count = 0;
        try {
            while(input.read() != null) {
                count++;
            }
        } catch(Exception e) {
            assertEquals(count, 2);
        }
    }

}
