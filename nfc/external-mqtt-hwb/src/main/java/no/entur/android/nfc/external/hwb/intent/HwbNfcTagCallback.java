package no.entur.android.nfc.external.hwb.intent;

import no.entur.android.nfc.external.ExternalNfcReaderCallback;
import no.entur.android.nfc.external.ExternalNfcTagCallback;

public interface HwbNfcTagCallback extends ExternalNfcTagCallback {

    public static final String EXTRA_HWB_TOKEN = ExternalNfcReaderCallback.class.getName() + ".extra.HWB_TOKEN";
    public static final String EXTRA_HWB_DEVICE_ID = ExternalNfcReaderCallback.class.getName() + ".extra.HWB_DEVICE_ID";
    public static final String EXTRA_HWB_CARD_CONTENT = ExternalNfcReaderCallback.class.getName() + ".extra.CARD_CONTENT";

    public static final String EXTRA_HWB_TRAVEL_CARD_NUMBER = ExternalNfcReaderCallback.class.getName() + ".extra.TRAVEL_CARD";

    public static final String EXTRA_TRACE_ID = ExternalNfcReaderCallback.class.getName() + ".extra.TRACE_ID";

}
