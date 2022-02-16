package no.entur.android.nfc.hce;

import no.entur.android.nfc.CommandAPDU;
import no.entur.android.nfc.ResponseAPDU;

public interface ResponseApduProtocol {

	byte getVersion();

	/**
	 *
	 * React to a command interaction from an initiator.
	 *
	 * @param command last command
	 *
	 * @return the next response, or null if no more commands expected
	 *
	 */

	ResponseAPDU handleCommandApdu(CommandAPDU command);

	/**
	 *
	 * Clear state, if any.
	 *
	 */

	void reset();

	void close();
}
