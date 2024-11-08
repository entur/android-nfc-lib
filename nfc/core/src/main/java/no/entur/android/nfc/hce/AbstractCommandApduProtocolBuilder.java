package no.entur.android.nfc.hce;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import no.entur.android.nfc.wrapper.tech.IsoDep;
import no.entur.android.nfc.wrapper.tech.IsoDepWrapper;

/**
 *
 * * Utility for helping the initiator negotiating the correct protocol version.
 *
 */

public abstract class AbstractCommandApduProtocolBuilder<B extends AbstractCommandApduProtocolBuilder<B>> {

	private static final String TAG = AbstractCommandApduProtocolBuilder.class.getName();

	protected IsoDep isoDep;
	protected List<CommandApduProtocol> protocols;
	protected byte[] applicationIdentifier; // https://stackoverflow.com/questions/27533193/android-hce-are-there-rules-for-aid
	protected int timeout = -1;

	// previous select application command
	protected byte[] selectApplicationResponse;

	/** In order of preference */
	public B withProtocols(CommandApduProtocol... protocols) {
		this.protocols = Arrays.asList(protocols);
		return (B)this;
	}

	public AbstractCommandApduProtocolBuilder withProtocols(List<CommandApduProtocol> protocols) {
		this.protocols = protocols;
		return (B)this;
	}

	public AbstractCommandApduProtocolBuilder withSelectApplicationResponse(byte[] response) {
		this.selectApplicationResponse = response;
		return (B)this;
	}

	public AbstractCommandApduProtocolBuilder withIsoDep(IsoDep isoDep) {
		this.isoDep = isoDep;
		return (B)this;
	}

	public AbstractCommandApduProtocolBuilder withIsoDep(android.nfc.tech.IsoDep isoDep) {
		return (B)withIsoDep(new IsoDepWrapper(isoDep));
	}

	public AbstractCommandApduProtocolBuilder withApplicationIdentifier(byte[] aid) {
		this.applicationIdentifier = aid;
		return (B)this;
	}

	public AbstractCommandApduProtocolBuilder withTimeout(int timeout) {
		this.timeout = timeout;
		return (B)this;
	}

	public abstract CommandApduProtocol build() throws IOException;

}
