package no.entur.android.nfc.hce.protocol;

import android.os.Parcelable;
import android.util.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import no.entur.android.nfc.util.ByteArrayHexStringConverter;
import no.entur.android.nfc.CommandAPDU;
import no.entur.android.nfc.hce.ResponseApduProtocol;
import no.entur.android.nfc.wrapper.Tag;
import no.entur.android.nfc.wrapper.tech.IsoDep;

/**
 *
 * Command bridge for testing.
 *
 */

public class ResponseAdpuProtocolIsoDep extends IsoDep {

	private final ResponseApduProtocol target;
	private boolean connected = false;

	public ResponseAdpuProtocolIsoDep(ResponseApduProtocol target) {
		this.target = target;
	}

	@Override
	public void setTimeout(int timeout) {
		// Not supported
	}

	@Override
	public int getTimeout() {
		return 1000;
	}

	@Override
	public byte[] getHistoricalBytes() {
		throw new RuntimeException();
	}

	@Override
	public byte[] getHiLayerResponse() {
		throw new RuntimeException();
	}

	@Override
	public byte[] transceive(byte[] data) throws IOException {
		return target.handleCommandApdu(new CommandAPDU(data)).getBytes();
	}

    @Override
    public <T> T transceive(Parcelable data) throws IOException {
        throw new RuntimeException();
    }

    @Override
    public <T> T transceiveMetadata(Parcelable c) throws IOException {
        return null;
    }

    @Override
	public int getMaxTransceiveLength() {
		return 256;
	}

	@Override
	public boolean isExtendedLengthApduSupported() {
		throw new RuntimeException();
	}

	@Override
	public boolean isNative() {
		throw new RuntimeException();
	}

	@Override
	public Tag getTag() {
		throw new RuntimeException();
	}

	@Override
	public void connect() throws IOException {
		connected = true;
	}

	@Override
	public void close() throws IOException {
		connected = false;
	}

	@Override
	public boolean isConnected() {
		return connected;
	}
}
