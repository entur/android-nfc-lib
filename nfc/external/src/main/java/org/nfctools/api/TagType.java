package org.nfctools.api;

public enum TagType {
	/**
	 * Unkown tag
	 */
	UNKNOWN("Unknown"),
	/**
	 * Mifare Classic with 1k memory
	 */
	MIFARE_CLASSIC_1K("Mifare Classic 1K"),

	/**
	 * Mifare Classic with 4k memory
	 */
	MIFARE_CLASSIC_4K("Mifare Classic 4K"),

	MIFARE_PLUS_SL1_2K("Mifar Plus SL1 2K"),
	MIFARE_PLUS_SL1_4K("Mifar Plus SL1 4K"),
	MIFARE_PLUS_SL2_2K("Mifar Plus SL2 2K"),
	MIFARE_PLUS_SL2_4K("Mifar Plus SL2 4K"),

	MIFARE_ULTRALIGHT("Mifare Ultralight"),

	MIFARE_ULTRALIGHT_C("Mifare Ultralight C"),

	MIFARE_MINI("Mifare Mini"),

	TOPAZ_JEWEL("Topaz Jewel"),

	FELICA("FeliCa"),
	FELICA_212K("FeliCa 212K"),
	FELICA_424K("FeliCa 424K"),
	/**
	 * Tag with NFCIP (P2P) capabilities
	 */
	NFCIP("P2NFCIP"),

	DESFIRE_EV1("DESfire EV1"),

	ISO_DEP("ISO_DEP"),

	ISO_14443_TYPE_A("RFID - ISO 14443 Type A - Android"),

	INFINEON_MIFARE_SLE_1K("Infineon Mifare SLE 66R35"),

	;

	private final String name;

	private TagType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}


}
