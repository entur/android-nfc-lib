package no.entur.android.nfc.wrapper;

import no.entur.android.nfc.wrapper.TagImpl;

/**
 * @hide
 */
interface INfcUnlockHandler {

    boolean onUnlockAttempted(in TagImpl tag);

}
