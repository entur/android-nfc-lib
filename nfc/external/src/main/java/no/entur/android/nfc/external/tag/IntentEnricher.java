package no.entur.android.nfc.external.tag;

import android.content.Intent;

import java.util.function.Function;

@FunctionalInterface
public interface IntentEnricher {

    Intent enrich(Intent intent);

    static IntentEnricher identity() {
        return t -> t;
    }
}
