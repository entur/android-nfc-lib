package no.entur.android.nfc.external.acs.tag;

import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;

import org.nfctools.api.ApduTag;
import org.nfctools.mf.block.MfBlock;
import org.nfctools.mf.ul.MfUlReaderWriter;
import org.nfctools.spi.acs.AcrMfUlReaderWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.entur.android.nfc.external.ExternalNfcTagCallback;
import no.entur.android.nfc.external.tag.MifareUltralightTagFactory;
import no.entur.android.nfc.external.acs.reader.command.ACRCommands;
import no.entur.android.nfc.external.acs.reader.command.ACSIsoDepWrapper;
import no.entur.android.nfc.external.tag.AbstractReaderIsoDepWrapper;

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
