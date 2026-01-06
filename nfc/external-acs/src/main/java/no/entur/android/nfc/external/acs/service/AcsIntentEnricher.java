package no.entur.android.nfc.external.acs.service;

import android.content.Intent;

import no.entur.android.nfc.external.ExternalNfcReaderCallback;
import no.entur.android.nfc.external.acs.reader.ReaderWrapper;
import no.entur.android.nfc.external.tag.IntentEnricher;

public class AcsIntentEnricher implements IntentEnricher {

    private final ReaderWrapper reader;

    public AcsIntentEnricher(ReaderWrapper reader) {
        this.reader = reader;
    }

    @Override
    public Intent enrich(Intent intent) {
        intent.putExtra(ExternalNfcReaderCallback.EXTRAS_READER_ID, reader.getId());
        return intent;
    }
}
