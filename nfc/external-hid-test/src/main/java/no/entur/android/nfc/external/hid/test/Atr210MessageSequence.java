package no.entur.android.nfc.external.hid.test;

import java.util.concurrent.atomic.AtomicInteger;

public class Atr210MessageSequence {

    private final AtomicInteger value = new AtomicInteger();

    public int next() {
        return value.incrementAndGet();
    }

}
