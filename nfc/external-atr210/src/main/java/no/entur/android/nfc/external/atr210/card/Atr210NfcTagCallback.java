package no.entur.android.nfc.external.atr210.card;

import no.entur.android.nfc.external.ExternalNfcReaderCallback;
import no.entur.android.nfc.external.ExternalNfcTagCallback;

public interface Atr210NfcTagCallback extends ExternalNfcTagCallback {

    public static final String EXTRA_PROVIDER_ID = ExternalNfcReaderCallback.class.getName() + ".extra.PROVIDER_ID";
    public static final String EXTRA_CLIENT_ID = ExternalNfcReaderCallback.class.getName() + ".extra.CLIENT_ID";

}
