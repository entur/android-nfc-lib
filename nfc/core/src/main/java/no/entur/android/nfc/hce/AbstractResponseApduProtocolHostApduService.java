package no.entur.android.nfc.hce;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import no.entur.android.nfc.CommandAPDU;
import no.entur.android.nfc.ResponseAPDU;

/**
 *
 * Utility for helping the target negotiating the correct protocol version.
 *
 * This utility uses a custom message protocol.
 *
 * Note that a client can ask for multiple application ids (AIDs), and a 'server' can have multiple AIDs, but only prefer a single service at a time.
 *
 */

public abstract class AbstractResponseApduProtocolHostApduService extends HostApduService {

	private static final String TAG = AbstractResponseApduProtocolHostApduService.class.getName();

	public final static int SELECT_APPLICATION_COMMAND = 0xA4;//
	protected static final byte[] RESPONSE_OK = new byte[] { (byte) 0x90, (byte) 0x00 };
	protected static final byte[] FUNCTION_NOT_SUPPORTED_RESPONSE = new byte[] { (byte) 0x68, (byte) 0x00 }; // function not supported
	protected static final byte[] WRONG_LENGTH_RESPONSE = new byte[] { (byte) 0x67, (byte) 0x00 }; // wrong length

	protected final List<ResponseApduProtocol> protocols = new ArrayList<>();
	protected ResponseApduProtocol protocol;

	protected void add(ResponseApduProtocol protocol) {
		this.protocols.add(protocol);
	}

	@Override
	public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {

		CommandAPDU command;
		try {
			command = new CommandAPDU(commandApdu);
		} catch (IllegalArgumentException e) {
			// malformed APDU, i.e. too short or Lc / Le does not match the length
			return WRONG_LENGTH_RESPONSE;
		}

		int ins = command.getINS();

		if (ins == SELECT_APPLICATION_COMMAND) {

			// respond with ok, so android understands
			// it can route the rest of the NFC commands here for the current NFC session,
			// if not (?) another application is selected.

			// Seems it is okey to add a payload in the response here
			// https://cardwerk.com/smart-card-standard-iso7816-4-section-6-basic-interindustry-commands/
			// 6.11.4 Response message (nominal case)
			// If the Le field contains only zeroes, then within the limit of 256 for short length or 65536 for extended length, all the bytes corresponding to
			// the selection option should be returned.

			onApplicationSelected();

			return handleApplicationSelectedCommandApdu();
		}

		if (protocol != null) {
			ResponseAPDU response = protocol.handleCommandApdu(command);
			if (response == null) {
				// no more commands expected
				return FUNCTION_NOT_SUPPORTED_RESPONSE;
			}

			return response.getBytes();
		}

		return handleCommandApdu(command);
	}

	protected abstract byte[] handleCommandApdu(CommandAPDU command);

	@NonNull
	protected abstract byte[] handleApplicationSelectedCommandApdu();

	@Override
	public void onDeactivated(int reason) {
		if (protocol != null) {
			protocol.close();
			protocol = null;
		}
	}

	public ResponseAPDU processCommandApdu(CommandAPDU commandApdu, Bundle extras) {
		return new ResponseAPDU(processCommandApdu(commandApdu.getBytes(), extras));
	}

	protected void onApplicationSelected() {
		// a new session, close the previous protocol (if any)
		if (protocol != null) {
			protocol.close();
			protocol = null;
		}
	}

}
