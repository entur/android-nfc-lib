package no.entur.abt.nfc.example.ultralight;

/**
 * NTAG213/215/216 can mirror the UID and/or the NFC counter (as ASCII hex) into the user memory. The NFC counter is incremented
 * on the first READ after the tag is powered, so the memory differs from one tap to the next.
 */

public class NtagMirror {

	private static final int UID_LENGTH = 14;
	private static final int COUNTER_LENGTH = 6;

	/**
	 * @param family tag family
	 * @return page of CFG0, or -1 if the tag does not support the NFC counter mirror
	 */

	public static int getConfigurationPage(TagFamily family) {
		switch (family) {
		case NTAG213:
			return 0x29;
		case NTAG215:
			return 0x83;
		case NTAG216:
			return 0xE3;
		default:
			return -1;
		}
	}

	private final int page;
	private final int offset;
	private final int length;

	private NtagMirror(int page, int offset, int length) {
		this.page = page;
		this.offset = offset;
		this.length = length;
	}

	/**
	 * @param family tag family
	 * @param result scan with the configuration pages
	 * @return the NFC counter mirror, or null if not enabled (or not known)
	 */

	public static NtagMirror counter(TagFamily family, ScanResult result) {
		int configurationPage = getConfigurationPage(family);
		if (configurationPage == -1) {
			return null;
		}
		byte[] cfg0 = result.getPage(configurationPage);
		byte[] cfg1 = result.getPage(configurationPage + 1);
		if (cfg0 == null || cfg1 == null) {
			return null;
		}
		int mirrorConfiguration = (cfg0[0] & 0xC0) >> 6; // 00 no mirror, 01 UID, 10 NFC counter, 11 UID and NFC counter
		int mirrorByte = (cfg0[0] & 0x30) >> 4;
		int mirrorPage = cfg0[2] & 0xFF;
		boolean counterEnabled = (cfg1[0] & 0x10) != 0; // NFC_CNT_EN

		if (!counterEnabled || mirrorPage < 4) {
			return null;
		}
		int start = mirrorPage * 4 + mirrorByte;
		switch (mirrorConfiguration) {
		case 2:
			return new NtagMirror(mirrorPage, mirrorByte, COUNTER_LENGTH);
		case 3:
			// UID, x, NFC counter
			int counterStart = start + UID_LENGTH + 1;
			return new NtagMirror(counterStart / 4, counterStart % 4, COUNTER_LENGTH);
		default:
			return null;
		}
	}

	/**
	 * @param memoryOffset offset in memory
	 * @return true if the byte is part of the NFC counter mirror
	 */

	public boolean contains(int memoryOffset) {
		int start = page * 4 + offset;
		return memoryOffset >= start && memoryOffset < start + length;
	}

	@Override
	public String toString() {
		return "NFC counter mirror at page " + page + " byte " + offset;
	}
}
