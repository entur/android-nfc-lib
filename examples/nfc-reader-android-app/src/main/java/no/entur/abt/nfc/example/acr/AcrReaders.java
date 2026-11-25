package no.entur.abt.nfc.example.acr;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import no.entur.android.nfc.external.acs.reader.Acr122UReader;
import no.entur.android.nfc.external.acs.reader.Acr1222LReader;
import no.entur.android.nfc.external.acs.reader.Acr1251UReader;
import no.entur.android.nfc.external.acs.reader.Acr1252UReader;
import no.entur.android.nfc.external.acs.reader.Acr1255UReader;
import no.entur.android.nfc.external.acs.reader.Acr1281UReader;
import no.entur.android.nfc.external.acs.reader.Acr1283LReader;
import no.entur.android.nfc.external.acs.reader.Acr1552UReader;
import no.entur.android.nfc.external.acs.reader.AcrReader;

/**
 * Open ACS readers, from the reader opened / closed broadcasts.
 */

public class AcrReaders {

	private static final Map<String, AcrReader> readers = new LinkedHashMap<>();

	private AcrReaders() {
	}

	public static synchronized void opened(AcrReader reader) {
		readers.put(reader.getId(), reader);
	}

	public static synchronized void closed(String readerId) {
		readers.remove(readerId);
	}

	public static synchronized List<AcrReader> getReaders() {
		return new ArrayList<>(readers.values());
	}

	/**
	 * @param reader reader
	 * @return the controls activity for the reader, or null if none
	 */

	public static Class<?> getControlsActivity(AcrReader reader) {
		if (reader instanceof Acr122UReader) {
			return Acr122UControlsActivity.class;
		} else if (reader instanceof Acr1222LReader) {
			return Acr1222LControlsActivity.class;
		} else if (reader instanceof Acr1251UReader) {
			return Acr1251UControlsActivity.class;
		} else if (reader instanceof Acr1252UReader) {
			return Acr1252UControlsActivity.class;
		} else if (reader instanceof Acr1255UReader) {
			return Acr1255UControlsActivity.class;
		} else if (reader instanceof Acr1281UReader) {
			return Acr1281UControlsActivity.class;
		} else if (reader instanceof Acr1283LReader) {
			return Acr1283LControlsActivity.class;
		} else if (reader instanceof Acr1552UReader) {
			return Acr1552UControlsActivity.class;
		}
		return null;
	}
}
