package no.entur.android.nfc.hce;

import no.entur.android.nfc.util.ByteArrayHexStringConverter;

public class ApplicationIdentifierUnknownException extends ApduProtocolException {

	private final byte[] aid;

	public ApplicationIdentifierUnknownException(byte[] aid) {
		super(ByteArrayHexStringConverter.toHexString(aid));

		this.aid = aid;
	}

	public byte[] getAid() {
		return aid;
	}
}
