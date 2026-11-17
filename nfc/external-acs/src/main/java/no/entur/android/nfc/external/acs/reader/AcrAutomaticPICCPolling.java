package no.entur.android.nfc.external.acs.reader;

import java.util.ArrayList;
import java.util.List;

/**
 * Automatic PICC polling setting (E0 00 00 23) of the ACR1251U, ACR1252U, ACR1255U, ACR1281U and ACR1283L. Bits 3 and 6 depend on
 * the reader and its firmware.
 *
 * @author thomas
 *
 */

public enum AcrAutomaticPICCPolling {

	AUTO_PICC_POLLING(1 << 0),
	TURN_ANTENNA_FIELD_IF_NO_PICC_FOUND(1 << 1),
	TURN_ANTENNA_FIELD_IF_PICC_IS_INACTIVE(1 << 2),

	/**
	 * ACR1252U API manual 1.08: activate the PICC when detected. RFU in the ACR1252U API manual 1.17, and in the ACR1251U, ACR1255U-J1
	 * and ACR1281U manuals.
	 */
	ACTIVATE_PICC_WHEN_DETECTED(1 << 3),
	/**
	 * The same bit as {@link #ACTIVATE_PICC_WHEN_DETECTED}, used for the ACR1255U. RFU in the ACR1255U-J1 reference manual 1.13.
	 */
	POLLING_ENABLE_EXTRA_MIFARE_TYPE_IDENTIFICATION(1 << 3),

	PICC_POLLING_INTERVAL_250(0x3 << 4, 0x00 << 4),
	PICC_POLLING_INTERVAL_500(0x3 << 4, 0x01 << 4),
	PICC_POLLING_INTERVAL_1000(0x3 << 4, 0x02 << 4),
	PICC_POLLING_INTERVAL_2500(0x3 << 4, 0x03 << 4),
	/**
	 * ACR1252U firmware 111.1 and later: NXP MIFARE ISO/IEC 14443 PICC selection, SAK 28h is recognized as MIFARE Classic 1K and 38h
	 * as MIFARE Classic 4K. RFU on the other readers.
	 */
	NXP_MIFARE_PICC_SELECTION(1 << 6),
	ENFORCE_ISO14443A_PART_4(1 << 7);

	private final int filter;
	private final int value;

	private AcrAutomaticPICCPolling(int filter) {
		this(filter, filter);
	}

	private AcrAutomaticPICCPolling(int filter, int value) {
		this.filter = filter;
		this.value = value;
	}

	public int getFilter() {
		return filter;
	}

	public int getValue() {
		return value;
	}

	public static List<AcrAutomaticPICCPolling> parse(int picc) {
		ArrayList<AcrAutomaticPICCPolling> values = new ArrayList<AcrAutomaticPICCPolling>();

		for (AcrAutomaticPICCPolling acrPICC : values()) {
			if ((picc & acrPICC.getFilter()) == acrPICC.getValue()) {
				values.add(acrPICC);
			}
		}

		return values;
	}

	public static int serialize(AcrAutomaticPICCPolling[] acrPICCs) {
		int value = 0;

		for (AcrAutomaticPICCPolling acrPICC : acrPICCs) {
			value |= acrPICC.getValue();
		}

		return value;
	}

	private static final AcrAutomaticPICCPolling[] ACR_1552 = new AcrAutomaticPICCPolling[]{
			AcrAutomaticPICCPolling.AUTO_PICC_POLLING,
			AcrAutomaticPICCPolling.TURN_ANTENNA_FIELD_IF_NO_PICC_FOUND,
			AcrAutomaticPICCPolling.POLLING_ENABLE_EXTRA_MIFARE_TYPE_IDENTIFICATION,
			AcrAutomaticPICCPolling.PICC_POLLING_INTERVAL_250,
			AcrAutomaticPICCPolling.PICC_POLLING_INTERVAL_500,
			AcrAutomaticPICCPolling.PICC_POLLING_INTERVAL_1000,
			AcrAutomaticPICCPolling.PICC_POLLING_INTERVAL_2500,
			AcrAutomaticPICCPolling.ENFORCE_ISO14443A_PART_4,
	};

	public static List<AcrAutomaticPICCPolling> parse1552(int picc) {
		ArrayList<AcrAutomaticPICCPolling> parse = new ArrayList<AcrAutomaticPICCPolling>();

		for (AcrAutomaticPICCPolling acrPICC : ACR_1552) {
			if ((picc & acrPICC.getFilter()) == acrPICC.getValue()) {
				parse.add(acrPICC);
			}
		}
		return parse;
	}

}
