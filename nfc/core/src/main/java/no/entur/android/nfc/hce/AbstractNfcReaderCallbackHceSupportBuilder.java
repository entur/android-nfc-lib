package no.entur.android.nfc.hce;

import android.app.Activity;

import java.util.Arrays;
import java.util.List;

import no.entur.android.nfc.wrapper.ReaderCallback;

@Deprecated // rather use NfcTargetAnalyzer approach
public abstract class AbstractNfcReaderCallbackHceSupportBuilder<B extends AbstractNfcReaderCallbackHceSupportBuilder<B>> {

	private static final String TAG = AbstractNfcReaderCallbackHceSupportBuilder.class.getName();

	protected AbstractNfcReaderCallbackHceSupport.Listener listener;
	protected Activity activity;
	protected byte[] applicationIdentifier;
	protected List<CommandApduProtocol> protocols;
	protected ReaderCallback callback;
	protected Integer transceiveTimeout;
	protected Integer presenceCheckDelay;

	public B withPresenceCheckDelay(Integer value) {
		this.presenceCheckDelay = value;
		return (B)this;
	}

	public B withProtocols(CommandApduProtocol... protocols) {
		return withProtocols(Arrays.asList(protocols));
	}

	public B withProtocols(List<CommandApduProtocol> protocols) {
		this.protocols = protocols;
		return (B)this;
	}

	public B withTransceiveTimeout(Integer timeout) {
		this.transceiveTimeout = timeout;
		return (B)this;
	}

	public B withActivity(Activity activity) {
		this.activity = activity;
		return (B)this;
	}

	public B withReaderCallbackDelegate(ReaderCallback callback) {
		this.callback = callback;
		return (B)this;
	}

	public B withListener(AbstractNfcReaderCallbackHceSupport.Listener listener) {
		this.listener = listener;
		return (B)this;
	}

	public B withApplicationIdentifier(byte[] aid) {
		this.applicationIdentifier = aid;
		return (B)this;
	}

	public <S extends AbstractNfcReaderCallbackHceSupport> S build() {
		if (listener == null) {
			throw new IllegalArgumentException();
		}
		if (activity == null) {
			throw new IllegalArgumentException();
		}
		if (applicationIdentifier == null) {
			throw new IllegalArgumentException();
		}
		if (protocols == null) {
			throw new IllegalArgumentException();
		}
		if (transceiveTimeout == null) {
			throw new IllegalArgumentException();
		}

		return buildNfcReaderCallbackHceSupport();
	}

	protected abstract <S extends AbstractNfcReaderCallbackHceSupport> S buildNfcReaderCallbackHceSupport();

}
