package no.entur.android.nfc.external.minova.service;

import android.content.Intent;

import no.entur.android.nfc.external.ExternalNfcReaderCallback;
import no.entur.android.nfc.external.tag.IntentEnricher;

public class MinovaIntentEnricher implements IntentEnricher {

    private final String ip;

    public MinovaIntentEnricher(String ip) {
        this.ip = ip;
    }

    @Override
    public Intent enrich(Intent intent) {
        intent.putExtra(MinovaService.EXTRA_IP, ip);
        intent.putExtra(ExternalNfcReaderCallback.EXTRAS_READER_ID, "minova-" + ip);
        return intent;
    }
}
