package no.entur.android.nfc.external.acs.tag;

import no.entur.android.nfc.external.acs.reader.command.ACSIsoDepWrapper;
import no.entur.android.nfc.external.tag.AbstractReaderIsoDepWrapper;
import no.entur.android.nfc.external.tag.TransceiveResultExceptionMapper;
import no.entur.android.nfc.wrapper.tech.TagTechnology;

public class PN532NfcAAdapter extends PN532DefaultTechnology {

	protected static final String TAG = PN532NfcAAdapter.class.getName();

	public PN532NfcAAdapter(AbstractReaderIsoDepWrapper reader, boolean print, TransceiveResultExceptionMapper exceptionMapper) {
		super(TagTechnology.NFC_A, reader, print, exceptionMapper);
	}

	public String toString() {
		return PN532NfcAAdapter.class.getSimpleName();
	}

}
