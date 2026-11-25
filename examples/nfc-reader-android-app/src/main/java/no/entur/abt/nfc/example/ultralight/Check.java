package no.entur.abt.nfc.example.ultralight;

/**
 * Checks, in the order they are shown.
 */

public enum Check {

	READ("READ pages 0-3", RawTechnology.NONE),
	UID("UID in pages 0-1", RawTechnology.NONE),
	BCC("BCC0 / BCC1", RawTechnology.NONE),
	MEMORY("READ all pages", RawTechnology.NONE),
	GET_VERSION("GET_VERSION (MifareUltralight)", RawTechnology.MIFARE_ULTRALIGHT),
	READ_SIG("READ_SIG (MifareUltralight)", RawTechnology.MIFARE_ULTRALIGHT),
	NFCA_GET_VERSION("GET_VERSION (NfcA)", RawTechnology.NFC_A),
	NFCA_FAST_READ("FAST_READ 0-3 (NfcA)", RawTechnology.NFC_A),
	AUTH1("AUTHENTICATE part 1 (NfcA)", RawTechnology.NFC_A);

	public enum RawTechnology {
		/** READ via MifareUltralight.readPages(..) */
		NONE,
		/** raw command via MifareUltralight.transceive(..) */
		MIFARE_ULTRALIGHT,
		/** raw command via NfcA.transceive(..) */
		NFC_A
	}

	private final String label;
	private final RawTechnology rawTechnology;

	Check(String label, RawTechnology rawTechnology) {
		this.label = label;
		this.rawTechnology = rawTechnology;
	}

	public String getLabel() {
		return label;
	}

	public RawTechnology getRawTechnology() {
		return rawTechnology;
	}

	public boolean isRaw() {
		return rawTechnology != RawTechnology.NONE;
	}
}
