package no.entur.android.nfc.external.minova.reader;

import android.os.RemoteException;
import android.util.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import no.entur.android.nfc.external.minova.IMcr0XReaderControl;
import no.entur.android.nfc.external.remote.RemoteCommandWriter;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;

public class IMcr0XBinder extends IMcr0XReaderControl.Stub {

	private static final Logger LOGGER = LoggerFactory.getLogger(IMcr0XBinder.class);

	private IMcr0XRemoteCommandWriter wrapper;

	public IMcr0XBinder() {
		attachInterface(this, IMcr0XBinder.class.getName());
	}

	public void setCommands(MinovaCommands reader) {
		wrapper = new IMcr0XRemoteCommandWriter(reader);
	}

	public void clearReader() {
		this.wrapper = null;
	}

	/*public byte[] getFirmware() {
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.getFirmware();
	}*/

	private byte[] noReaderException() {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream(out);

			dout.writeInt(RemoteCommandWriter.VERSION);
			dout.writeInt(RemoteCommandWriter.STATUS_EXCEPTION);
			dout.writeUTF("Reader not connected");

			byte[] response = out.toByteArray();

			LOGGER.debug("Send exception response length " + response.length + ":" + ByteArrayHexStringConverter.toHexString(response));

			return response;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte[] getIp() throws RemoteException {
		return wrapper.getIp();
	}

	@Override
	public byte[] buzz(int durationInMillis, int times) throws RemoteException {
		return wrapper.buzz(durationInMillis, times);
	}

	@Override
	public byte[] displayText(int xAxis, int yAxis, int font, String text) throws RemoteException {
		return wrapper.displayText(xAxis, yAxis, font, text);
	}

	@Override
	public byte[] displayTextWithDuration(int xAxis, int yAxis, int font, String text, int durationInMillis) throws RemoteException {
		return wrapper.displayTextWithDuration(xAxis, yAxis, font, text, durationInMillis);
	}
}
