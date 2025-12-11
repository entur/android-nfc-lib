package no.entur.android.nfc.external.hwb.intent;

import no.entur.android.nfc.external.ExternalNfcReaderCallback;
import no.entur.android.nfc.external.ExternalNfcTagCallback;

public interface ExternalHfcAtr210TagCallback extends ExternalNfcTagCallback {

    public static final String EXTRA_HWB_DEVICE_ID = ExternalNfcReaderCallback.class.getName() + ".extra.DEVICE_ID";

}
