package no.entur.android.nfc.external;

import android.content.Intent;

import no.entur.android.nfc.wrapper.ReaderCallback;
import no.entur.android.nfc.wrapper.Tag;

public interface ExternalNfcTagCallback extends ReaderCallback {

	/** Action corresponding to {@linkplain android.nfc.NfcAdapter#ACTION_NDEF_DISCOVERED}. */
	public static final String ACTION_NDEF_DISCOVERED = ExternalNfcTagCallback.class.getName() + ".action.NDEF_DISCOVERED";
	/** Action corresponding to {@linkplain android.nfc.NfcAdapter#ACTION_TAG_DISCOVERED}. */
	public static final String ACTION_TAG_DISCOVERED = ExternalNfcTagCallback.class.getName() + ".action.TAG_DISCOVERED";
	/** Action corresponding to {@linkplain android.nfc.NfcAdapter#ACTION_TECH_DISCOVERED}. */
	public static final String ACTION_TECH_DISCOVERED = ExternalNfcTagCallback.class.getName() + ".action.TECH_DISCOVERED";
	/** Action corresponding to hidden {@linkplain android.nfc.NfcAdapter#ACTION_TAG_LEFT_FIELD}. */
	public static final String ACTION_TAG_LEFT_FIELD = ExternalNfcTagCallback.class.getName() + ".action.TAG_LEFT_FIELD";

    public static final String EXTRAS_TAG_HANDLE = ExternalNfcTagCallback.class.getName() + ".extra.EXTRAS_TAG_HANDLE";

    /**
	 *
	 * Reader callback for external tags.
	 *
	 * @param tag tag discovered
	 * @param intent can be null
	 */

	default void onExternalTagDiscovered(Tag tag, Intent intent) {
		onTagDiscovered(tag, intent);
	}

}
