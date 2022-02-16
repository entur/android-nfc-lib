package no.entur.android.nfc.hce;

import java.io.IOException;

import no.entur.android.nfc.wrapper.tech.IsoDep;

public interface CommandApduProtocol {

	byte getVersion();

	void close() throws IOException;

	void initialize(IsoDep isoDep, byte[] content);
}
