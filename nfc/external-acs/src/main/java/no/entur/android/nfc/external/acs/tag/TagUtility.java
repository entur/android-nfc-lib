package no.entur.android.nfc.external.acs.tag;

import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.util.Log;

import org.nfctools.api.ApduTag;
import org.nfctools.api.TagType;
import org.nfctools.mf.block.MfBlock;
import org.nfctools.mf.ul.MfUlReaderWriter;
import org.nfctools.spi.acs.AcrMfUlReaderWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.entur.android.nfc.external.ExternalNfcTagCallback;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;
import no.entur.android.nfc.external.acs.reader.command.ACRCommands;
import no.entur.android.nfc.external.acs.reader.command.ACSIsoDepWrapper;
import no.entur.android.nfc.external.tag.AbstractReaderIsoDepWrapper;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;

public class TagUtility {

	private static final Logger LOGGER = LoggerFactory.getLogger(TagUtility.class);

	// https://stackoverflow.com/questions/9514684/what-apdu-command-gets-card-id
	private static final byte[] GET_TAG_ID = new byte[] { (byte) 0xFF, (byte) 0xCA, 0x00, 0x00, 0x00 };

	public static void sendTagIdIndent(Context context, byte[] uid) {
		final Intent intent = new Intent(ExternalNfcTagCallback.ACTION_TAG_DISCOVERED);

		if (uid != null) {
			intent.putExtra(NfcAdapter.EXTRA_ID, uid);
		}

		context.sendBroadcast(intent, "android.permission.NFC");
	}

	public static void ultralight(Context context, ApduTag tag) {

		MfUlReaderWriter readerWriter = new AcrMfUlReaderWriter(tag);

		MfBlock[] initBlocks = null;

		byte[] uid;
		try {
			// get uid from first two blocks:
			// 3 bytes from index 0
			// 4 bytes from index 1

			initBlocks = readerWriter.readBlock(0, 2);

			uid = new byte[7];
			System.arraycopy(initBlocks[0].getData(), 0, uid, 0, 3);
			System.arraycopy(initBlocks[1].getData(), 0, uid, 3, 4);
		} catch (Exception e) {
			LOGGER.warn("Problem reading tag UID", e);
			uid = new byte[] { MifareUltralightTagFactory.NXP_MANUFACTURER_ID };
		}

		TagUtility.sendTagIdIndent(context, uid);
	}

	public static void desfire(Context context, ACSIsoDepWrapper wrapper) {

		byte[] uid;
		try {
			uid = getPcscUid(wrapper);
		} catch (Exception e) {
			LOGGER.debug("Problem getting manufacturer data", e);

			uid = null;
		}

		TagUtility.sendTagIdIndent(context, uid);
	}

	public static void sendTechBroadcast(Context context) {
		Intent intent = new Intent(ExternalNfcTagCallback.ACTION_TECH_DISCOVERED);

		context.sendBroadcast(intent, "android.permission.NFC");
	}

	public static boolean isBlank(byte[] uid) {
		for (byte b : uid) {
			if (b != (byte) 0x00) {
				return false;
			}
		}

		return true;
	}

	@SuppressWarnings("java:S3776")
	public static TagType identifyTagType(String name, byte[] historicalBytes) {

		if (name != null) {
			if (name.contains("1252") || name.contains("1255")) {
				if (historicalBytes.length >= 11) {
					LOGGER.debug(ByteArrayHexStringConverter.toHexString(historicalBytes));
					int tagId = (historicalBytes[13] & 0xff) << 8 | (historicalBytes[14] & 0xff);
					byte standard = historicalBytes[12];
					if (standard == 0x11) {
						// felicia
						switch (tagId) {
						case 0x003B:
							return TagType.FELICA_212K;
						case 0xF012:
							return TagType.FELICA_424K;
						default: {
							LOGGER.warn("Unknown tag id " + ByteArrayHexStringConverter.toHexString(new byte[] { historicalBytes[13], historicalBytes[14] })
									+ " (" + Integer.toHexString(tagId) + ")");
						}
						}
					} else {
						switch (tagId) {
						case 0x0001:
							return TagType.MIFARE_CLASSIC_1K;
						case 0x0002:
							return TagType.MIFARE_CLASSIC_4K;
						case 0x0003:
							return TagType.MIFARE_ULTRALIGHT;
						case 0x0026:
							return TagType.MIFARE_MINI;
						case 0x003A:
							return TagType.MIFARE_ULTRALIGHT_C;
						case 0x0036:
							return TagType.MIFARE_PLUS_SL1_2K;
						case 0x0037:
							return TagType.MIFARE_PLUS_SL1_4K;
						case 0x0038:
							return TagType.MIFARE_PLUS_SL2_2K;
						case 0x0039:
							return TagType.MIFARE_PLUS_SL2_4K;
						case 0x0030:
							return TagType.TOPAZ_JEWEL;
						case 0xFF40:
							return TagType.NFCIP;
						case 0xFF88:
							return TagType.INFINEON_MIFARE_SLE_1K;
						default: {

							if ((historicalBytes[13] & 0xFF) == 0xFF) {
								LOGGER.info(
										"Assume android device for "
												+ ByteArrayHexStringConverter.toHexString(new byte[] { historicalBytes[13], historicalBytes[14] }) + " ("
												+ Integer.toHexString(tagId) + ")");

								return TagType.ISO_14443_TYPE_A;
							} else {
								LOGGER.warn("Unknown tag id " + ByteArrayHexStringConverter.toHexString(new byte[] { historicalBytes[13], historicalBytes[14] })
										+ " (" + Integer.toHexString(tagId) + ")");
							}
						}
						}

					}

				}
			}
		}

		return TagType.identifyTagType(historicalBytes);
	}

	public static byte[] getPcscUid(AbstractReaderIsoDepWrapper wrapper) {
		try {
			byte[] response = wrapper.transceive(GET_TAG_ID);
			if (ACRCommands.isSuccessControl(response)) {
				byte[] uid = new byte[response.length - 2];
				System.arraycopy(response, 0, uid, 0, uid.length);
				return uid;
			}
		} catch (Exception e) {
			LOGGER.debug("Problem getting tag uid", e);
		}
		return null;
	}

}
