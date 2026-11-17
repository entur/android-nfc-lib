package no.entur.abt.nfc.example.ultralight;

/**
 * MIFARE Ultralight / NTAG product family, with the commands each family is expected to support. The tag types under test
 * are listed (in this order) whether or not they have been scanned.
 * <br>
 * Deliberately independent of the library (i.e. NfcNtagVersion), so that the test does not check the library with itself.
 */

public enum TagFamily {

	ULTRALIGHT("Ultralight (MF0ICU1)", 16, false, false, false, true),
	/** The key pages 0x2C - 0x2F cannot be read */
	ULTRALIGHT_C("Ultralight C (MF0ICU2)", 44, false, false, true, true),
	ULTRALIGHT_EV1_MF0UL11("Ultralight EV1 48 bytes (MF0UL11)", 20, true, true, null, true),
	ULTRALIGHT_EV1_MF0UL21("Ultralight EV1 128 bytes (MF0UL21)", 41, true, true, null, true),
	NTAG203("NTAG203", 42, false, false, false, true),
	NTAG210("NTAG210", 20, true, true, null, true),
	NTAG212("NTAG212", 41, true, true, null, true),
	NTAG213("NTAG213", 45, true, true, null, true),
	NTAG215("NTAG215", 135, true, true, null, true),
	NTAG216("NTAG216", 231, true, true, null, true),
	/** Answers GET_VERSION, but with an unknown product */
	OTHER_GET_VERSION("Other (answers GET_VERSION)", 16, true, null, null, false),
	UNKNOWN("Unknown (scan on the phone)", 16, null, null, null, false);

	private final String label;
	/** number of pages which can be read, starting at page 0 */
	private final int readablePages;
	private final Boolean getVersion;
	private final Boolean readSignatureAndFastRead;
	private final Boolean ultralightCAuthentication;
	/** whether the tag type is always listed */
	private final boolean listed;

	TagFamily(String label, int readablePages, Boolean getVersion, Boolean readSignatureAndFastRead, Boolean ultralightCAuthentication, boolean listed) {
		this.label = label;
		this.readablePages = readablePages;
		this.getVersion = getVersion;
		this.readSignatureAndFastRead = readSignatureAndFastRead;
		this.ultralightCAuthentication = ultralightCAuthentication;
		this.listed = listed;
	}

	public String getLabel() {
		return label;
	}

	public int getReadablePages() {
		return readablePages;
	}

	/**
	 * @return whether GET_VERSION is supported, or null if unknown
	 */
	public Boolean getGetVersion() {
		return getVersion;
	}

	/**
	 * @return whether READ_SIG and FAST_READ are supported, or null if unknown
	 */
	public Boolean getReadSignatureAndFastRead() {
		return readSignatureAndFastRead;
	}

	/**
	 * @return whether the (Ultralight C) AUTHENTICATE command is supported, or null if unknown / not applicable
	 */
	public Boolean getUltralightCAuthentication() {
		return ultralightCAuthentication;
	}

	public boolean isKnown() {
		return this != UNKNOWN;
	}

	public boolean isListed() {
		return listed;
	}

	/**
	 * Parse a GET_VERSION response: header, vendor, product type, product subtype, major version, minor version, storage size, protocol type.
	 *
	 * @param version GET_VERSION response
	 * @return tag family, never null
	 */

	public static TagFamily fromVersion(byte[] version) {
		if (version == null || version.length != 8 || version[1] != 0x04) {
			// not an NXP product we know
			return OTHER_GET_VERSION;
		}
		int productType = version[2] & 0xFF;
		int productSubtype = version[3] & 0xFF;
		int majorVersion = version[4] & 0xFF;
		int storageSize = version[6] & 0xFF;
		if (majorVersion != 0x01) {
			// NTAG21x and Ultralight EV1 are version 1, other products have other memory maps (i.e. NTAG I2C is version 2)
			return OTHER_GET_VERSION;
		}
		if (productType == 0x04) {
			// NTAG, per the NXP datasheets: NTAG210 / 212 are subtype 01 (17 pF), NTAG213 / 215 / 216 subtype 02 (50 pF);
			// other subtypes (i.e. NTAG213F / 216F, NTAG213 TT) have other memory maps
			switch (storageSize) {
			case 0x0B:
				return productSubtype == 0x01 ? NTAG210 : OTHER_GET_VERSION;
			case 0x0E:
				return productSubtype == 0x01 ? NTAG212 : OTHER_GET_VERSION;
			case 0x0F:
				return productSubtype == 0x02 ? NTAG213 : OTHER_GET_VERSION;
			case 0x11:
				return productSubtype == 0x02 ? NTAG215 : OTHER_GET_VERSION;
			case 0x13:
				return productSubtype == 0x02 ? NTAG216 : OTHER_GET_VERSION;
			default:
				return OTHER_GET_VERSION;
			}
		} else if (productType == 0x03) {
			// MIFARE Ultralight EV1, per the NXP datasheet subtype 01 (17 pF, MF0ULx1) or 02 (50 pF, MF0ULHx1)
			if (productSubtype != 0x01 && productSubtype != 0x02) {
				return OTHER_GET_VERSION;
			}
			switch (storageSize) {
			case 0x0B:
				return ULTRALIGHT_EV1_MF0UL11;
			case 0x0E:
				return ULTRALIGHT_EV1_MF0UL21;
			default:
				return OTHER_GET_VERSION;
			}
		}
		return OTHER_GET_VERSION;
	}
}
