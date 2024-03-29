package no.entur.android.nfc.websocket.android;

import no.entur.android.nfc.external.tag.AbstractReaderIsoDepWrapper;
import no.entur.android.nfc.external.tag.DefaultCommandTechnology;
import no.entur.android.nfc.external.tag.TransceiveResultExceptionMapper;
import no.entur.android.nfc.wrapper.tech.TagTechnology;

public class MifareUltralightDefaultCommandTechnology extends DefaultCommandTechnology {

	public MifareUltralightDefaultCommandTechnology(AbstractReaderIsoDepWrapper reader, boolean print, TransceiveResultExceptionMapper exceptionMapper) {
		super(TagTechnology.MIFARE_ULTRALIGHT, reader, print, exceptionMapper);
	}

	public String toString() {
		return MifareUltralightDefaultCommandTechnology.class.getSimpleName();
	}

}
