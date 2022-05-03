package no.entur.android.nfc.external;

import android.content.Intent;

import no.entur.android.nfc.wrapper.ReaderCallback;
import no.entur.android.nfc.wrapper.Tag;
import no.entur.android.nfc.wrapper.tech.Ndef;
import no.entur.android.nfc.wrapper.tech.NdefFormatable;

public interface ExternalNfcTagCallback extends ReaderCallback {

	/** Action corresponding to {@linkplain android.nfc.NfcAdapter#ACTION_NDEF_DISCOVERED}. */
	public static final String ACTION_NDEF_DISCOVERED = ExternalNfcTagCallback.class.getName() + ".action.NDEF_DISCOVERED";
	/** Action corresponding to {@linkplain android.nfc.NfcAdapter#ACTION_TAG_DISCOVERED}. */
	public static final String ACTION_TAG_DISCOVERED = ExternalNfcTagCallback.class.getName() + "action.TAG_DISCOVERED";
	/** Action corresponding to {@linkplain android.nfc.NfcAdapter#ACTION_TECH_DISCOVERED}. */
	public static final String ACTION_TECH_DISCOVERED = ExternalNfcTagCallback.class.getName() + ".action.TECH_DISCOVERED";
	/** Action corresponding to hidden {@linkplain android.nfc.NfcAdapter#ACTION_TAG_LEFT_FIELD}. */
	public static final String ACTION_TAG_LEFT_FIELD = ExternalNfcTagCallback.class.getName() + ".action.TAG_LEFT_FIELD";

	default void onExternalTagDiscovered(Tag tag) {
		onTagDiscovered(tag);
	}

}
