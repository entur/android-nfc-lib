package no.entur.abt.nfc.example.ultralight;

import java.util.EnumMap;
import java.util.Map;

/**
 * Result of scanning a tag once, on the phone or an external reader.
 */

public class ScanResult {

	public enum Source {
		PHONE, EXTERNAL
	}

	public Source source;
	/** identifies the reader, one column per reader */
	public String readerKey;
	/** short reader name, used as column title */
	public String readerLabel;
	public String readerName;
	public ReaderCapability capability;
	public long time;

	public byte[] uid;
	public String technologies;
	/** MifareUltralight.getType(), or null if no MifareUltralight technology */
	public Integer type;
	/** external only: NfcNtag.EXTRA_ULTRALIGHT_TYPE */
	public Integer ultralightTypeExtra;

	/** GET_VERSION response, if any */
	public byte[] version;
	/** family seen in this scan (GET_VERSION or Ultralight C authentication), or null */
	public TagFamily family;

	/** pages read, starting at page 0 */
	public byte[] memory = new byte[0];
	public int pagesRead;
	public int pagesExpected;

	public final Map<Check, Outcome> outcomes = new EnumMap<>(Check.class);

	/** tag was lost during the scan */
	public boolean lost;
	public String error;

	public Outcome get(Check check) {
		return outcomes.get(check);
	}

	public void set(Check check, Outcome outcome) {
		outcomes.put(check, outcome);
	}

	public byte[] getPage(int page) {
		if (page >= pagesRead) {
			return null;
		}
		byte[] result = new byte[4];
		System.arraycopy(memory, page * 4, result, 0, 4);
		return result;
	}
}
