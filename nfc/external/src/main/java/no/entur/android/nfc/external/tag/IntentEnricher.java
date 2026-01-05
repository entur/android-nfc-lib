package no.entur.android.nfc.external.tag;

import android.content.Intent;

@FunctionalInterface
public interface IntentEnricher {

    Intent enrich(Intent intent);

    static IntentEnricher identity() {
        return (t) -> t;
    }
}
