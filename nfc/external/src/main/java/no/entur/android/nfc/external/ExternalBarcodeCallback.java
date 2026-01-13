package no.entur.android.nfc.external;

import android.content.Intent;

public interface ExternalBarcodeCallback {

    public static final String ACTION_BARCODE_DISCOVERED = ExternalBarcodeCallback.class.getName() + ".action.BARCODE";
    public static final String BARCODE_EXTRA_BODY = ExternalBarcodeCallback.class.getName() + ".extra.BODY";

    /**
     *
     * Reader callback for barcodes
     *
     * @param body barcode byte representation
     * @param intent
     */

    default void onBarcodeDiscovered(byte[] body, Intent intent) {
    }

}
