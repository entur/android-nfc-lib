package no.entur.abt.nfc.example.ultralight;

import no.entur.android.nfc.external.acs.reader.ReaderWrapper;

/**
 * Which raw commands a reader is expected to send to the tag, per technology.
 */

public enum ReaderCapability {

	/** the built-in NFC reader, the reference */
	PHONE("phone (reference)", RawSupport.SUPPORTED, RawSupport.SUPPORTED),
	/** ACR1222L: PN53x InCommunicateThru pass-through, verified with this screen */
	ACS_RAW("ACS, raw commands via pass-through", RawSupport.SUPPORTED, RawSupport.SUPPORTED),
	/** other ACS readers which get the pass-through (ACR122U), not verified with this screen */
	ACS_UNVERIFIED_RAW("ACS, raw commands via pass-through, unverified reader model", RawSupport.UNVERIFIED, RawSupport.UNVERIFIED),
	/** ACS readers without the PN53x pass-through (i.e. ACR1252, ACR1255, ACR1283): the library does not send raw commands */
	ACS_NO_RAW("ACS, no raw commands (no PN53x pass-through)", RawSupport.NOT_SUPPORTED, RawSupport.NOT_SUPPORTED),
	/** ACS reader, but the model is not known (no reader opened broadcast seen), so no expectation for raw commands */
	ACS_UNKNOWN_MODEL("ACS, unknown model, reopen this screen or re-plug the reader", RawSupport.NOT_KNOWN, RawSupport.NOT_KNOWN),
	/** HID ATR210 via MQTT: MifareUltralight supports READ only, NfcA commands are forwarded as is */
	HID_ATR210("HID ATR210, MifareUltralight READ only, NfcA commands forwarded", RawSupport.NOT_SUPPORTED, RawSupport.NOT_KNOWN),
	/** WebSocket: commands are forwarded to the remote PC/SC reader */
	WEBSOCKET("WebSocket, commands forwarded to the remote PC/SC reader", RawSupport.NOT_KNOWN, RawSupport.NOT_KNOWN),
	UNKNOWN("unknown reader", RawSupport.NOT_KNOWN, RawSupport.NOT_KNOWN);

	public enum RawSupport {
		/** raw commands are sent to the tag */
		SUPPORTED,
		/** raw commands are sent to the tag, but this is not verified for the reader */
		UNVERIFIED,
		/** raw commands are not sent to the tag, i.e. always fail */
		NOT_SUPPORTED,
		/** no expectation */
		NOT_KNOWN
	}

	private final String label;
	private final RawSupport mifareUltralight;
	private final RawSupport nfcA;

	ReaderCapability(String label, RawSupport mifareUltralight, RawSupport nfcA) {
		this.label = label;
		this.mifareUltralight = mifareUltralight;
		this.nfcA = nfcA;
	}

	public String getLabel() {
		return label;
	}

	public RawSupport getSupport(Check check) {
		switch (check.getRawTechnology()) {
		case MIFARE_ULTRALIGHT:
			return mifareUltralight;
		case NFC_A:
			return nfcA;
		default:
			return RawSupport.SUPPORTED;
		}
	}

	/**
	 * Whether the library sends raw commands (see ReaderWrapper.supportsPassThrough(..)), and whether that is verified with this
	 * screen for the reader model.
	 *
	 * @param readerName ACS reader name, i.e. ACS ACR122U PICC Interface
	 * @return capability
	 */

	public static ReaderCapability fromAcsReaderName(String readerName) {
		if (readerName == null) {
			return ACS_UNKNOWN_MODEL;
		}
		if (!ReaderWrapper.supportsPassThrough(readerName)) {
			return ACS_NO_RAW;
		}
		if (readerName.contains("ACR1222")) {
			return ACS_RAW;
		}
		return ACS_UNVERIFIED_RAW;
	}
}
