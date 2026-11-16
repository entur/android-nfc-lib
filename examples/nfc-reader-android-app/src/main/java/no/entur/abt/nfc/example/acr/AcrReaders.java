package no.entur.abt.nfc.example.acr;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
}
