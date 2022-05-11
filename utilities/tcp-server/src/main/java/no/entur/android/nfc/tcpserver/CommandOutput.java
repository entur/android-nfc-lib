package no.entur.android.nfc.tcpserver;

import java.io.Closeable;
import java.io.IOException;

public interface CommandOutput<T> extends Closeable {

    void write(T command) throws IOException;
}
