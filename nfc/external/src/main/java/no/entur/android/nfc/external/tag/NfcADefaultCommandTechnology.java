package no.entur.android.nfc.external.tag;

import no.entur.android.nfc.wrapper.tech.TagTechnology;

public class NfcADefaultCommandTechnology extends DefaultCommandTechnology {

	protected static final String TAG = NfcADefaultCommandTechnology.class.getName();

	public NfcADefaultCommandTechnology(AbstractReaderIsoDepWrapper reader, boolean print) {
		super(TagTechnology.NFC_A, reader, print);
	}

	public String toString() {
		return NfcADefaultCommandTechnology.class.getSimpleName();
	}

}
