package no.entur.android.nfc.external.acs.reader.command.remote;

import android.util.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.entur.android.nfc.external.acs.reader.command.ACR1251Commands;
import no.entur.android.nfc.external.remote.RemoteCommandWriter;

public class IAcr1251UCommandWrapper extends AcrRemoteCommandWriter {

	private static final Logger LOGGER = LoggerFactory.getLogger(IAcr1251UCommandWrapper.class);

	private ACR1251Commands commands;

	public IAcr1251UCommandWrapper(ACR1251Commands commands) {
		this.commands = commands;
	}

	public byte[] getFirmware() {

		String firmware = null;
		Exception exception = null;
		try {
			firmware = commands.getFirmware(0);
		} catch (Exception e) {
			LOGGER.debug("Problem reading firmware", e);

			exception = e;
		}

		return returnValue(firmware, exception);
	}

	public byte[] getPICC() {
		Integer picc = null;
		Exception exception = null;
		try {
			picc = commands.getPICC(0);
		} catch (Exception e) {
			LOGGER.debug("Problem reading PICC", e);

			exception = e;
		}

		return returnValue(picc, exception);
	}

	public byte[] setPICC(int picc) {

		Boolean result = null;
		Exception exception = null;
		try {
			result = commands.setPICC(0, picc);
		} catch (Exception e) {
			LOGGER.debug("Problem setting PICC", e);

			exception = e;
		}

		return returnValue(result, exception);

	}

	public byte[] control(int slotNum, int controlCode, byte[] command) {

		byte[] value = null;
		Exception exception = null;
		try {
			value = commands.control(slotNum, controlCode, command);
		} catch (Exception e) {
			LOGGER.debug("Problem control", e);

			exception = e;
		}

		return returnValue(value, exception);
	}

	public byte[] transmit(int slotNum, byte[] command) {
		byte[] value = null;
		Exception exception = null;
		try {
			value = commands.transmit(slotNum, command);
		} catch (Exception e) {
			LOGGER.debug("Problem transmit", e);

			exception = e;
		}

		return returnValue(value, exception);
	}
}
