package no.entur.android.nfc.external.minova.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

import no.entur.android.nfc.tcpserver.CommandInput;

public class CommaCommandInput implements CommandInput<String> {

    private final InputStreamReader reader;

    public CommaCommandInput(InputStreamReader reader) {
        this.reader = reader;
    }

    @Override
    public String read() throws IOException {

        StringBuilder builder = new StringBuilder(128);
        BufferedReader bufRead = new BufferedReader(reader);
        String str;

        if ((str = bufRead.readLine()) == null) {
            return null;
        } else {
            builder.append(str);
        }

        return builder.toString();
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
