package no.entur.android.nfc.tcpserver;

import java.io.Closeable;
import java.io.IOException;

public interface CommandInput<T> extends Closeable {

    T read() throws IOException;

}
