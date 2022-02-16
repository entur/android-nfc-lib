package no.entur.android.nfc.util;

import java.io.IOException;

import no.entur.android.nfc.wrapper.Tag;
import no.entur.android.nfc.wrapper.tech.IsoDep;

/**
 *
 * Helper class for consistent handling of IsoDep targets.
 *
 * @param <T> expected return type
 */

public abstract class AbstractIsoDepProcessor<T> {

	private static final String TAG = AbstractIsoDepProcessor.class.getName();

	protected int transceiveTimeout;

	public AbstractIsoDepProcessor(int transceiveTimeout) {
		this.transceiveTimeout = transceiveTimeout;
	}

	public T onTag(Tag tag) throws Exception {
		IsoDep isoDep = IsoDep.get(tag);
		if (isoDep != null) {
			try {
				if (!isoDep.isConnected()) {
					isoDep.connect();
				}
				if (transceiveTimeout != -1) {
					isoDep.setTimeout(transceiveTimeout);
				}

				return onIsoDep(isoDep);
			} catch (IOException e) {
				return onIOException(e);
			} finally {
				try {
					isoDep.close();
				} catch (IOException ignored) {
					// ignore
				}
			}
		} else {
			return onOtherTagTechnology(tag);
		}
	}

	public abstract T onIsoDep(IsoDep isoDep) throws Exception;;

	public abstract T onIOException(IOException e) throws Exception;

	public abstract T onOtherTagTechnology(Tag tag) throws Exception;

}
