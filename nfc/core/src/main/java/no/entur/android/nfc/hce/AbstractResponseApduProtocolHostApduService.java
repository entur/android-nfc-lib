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

	protected final List<ResponseApduProtocol> protocols = new ArrayList<>();
	protected ResponseApduProtocol protocol;

	protected void add(ResponseApduProtocol protocol) {
		this.protocols.add(protocol);
	}

	@Override
	public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {

		CommandAPDU command = new CommandAPDU(commandApdu);

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

			return handleApplicationSelectedCommandAdpu();
		}

		if (protocol != null) {
			ResponseAPDU response = protocol.handleCommandApdu(command);

			return response.getBytes();
		}

		return handleCommandApdu(command);
	}

	protected abstract byte[] handleCommandApdu(CommandAPDU command);

	@NonNull
	protected abstract byte[] handleApplicationSelectedCommandAdpu();

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
		this.protocol = null; // just to make sure
	}

}
