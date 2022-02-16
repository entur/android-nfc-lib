package no.entur.android.nfc.external.service.tag;

import android.nfc.NdefMessage;
import android.os.RemoteException;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.List;

import no.entur.android.nfc.wrapper.ErrorCodes;
import no.entur.android.nfc.wrapper.INfcTag;
import no.entur.android.nfc.wrapper.TagImpl;
import no.entur.android.nfc.wrapper.TransceiveResult;

public class INFcTagBinder extends INfcTag.Stub {

	private static final String TAG = INFcTagBinder.class.getName();

	private static final String NO_READER_MSG = "No reader";

	private static final String UNSUPPORTED_OPERATION_MSG = "Unsupported operation";

	protected TagProxyStore store;

	protected ReaderTechnology readerTechnology;

	public INFcTagBinder(TagProxyStore store) {
		this.store = store;
	}

	public TagTechnology getConnected(int nativeHandle) {
		TagProxy proxy = store.get(nativeHandle);

		if (proxy == null) {
			return null;
		}

		return proxy.getCurrent();
	}

	public void setReaderTechnology(ReaderTechnology readerTechnology) {
		this.readerTechnology = readerTechnology;
	}

	public boolean canMakeReadOnly(int ndefType) throws RemoteException {
		// Log.d(TAG, "canMakeReadOnly");

		return readerTechnology.canMakeReadOnly(ndefType);
	}

	public int close(int nativeHandle) throws RemoteException {
		// Log.d(TAG, "close");

		TagProxy proxy = store.get(nativeHandle);

		if (proxy != null) {
			TagTechnology current = proxy.getCurrent();
			if (current != null) {
				proxy.setCurrent(null);
			}
		}

		return ErrorCodes.ERROR_DISCONNECT;
	}

	public int connect(int serviceHandle, int technology) throws RemoteException {
		// Log.d(TAG, "connect");

		TagProxy proxy = store.get(serviceHandle);

		if (proxy == null) {
			Log.d(TAG, "No proxy for " + serviceHandle);

			return ErrorCodes.ERROR_CONNECT;
		}

		if (!proxy.selectTechnology(technology)) {
			Log.d(TAG, "No technology " + technology + " for " + serviceHandle + ": " + proxy.getTechnologies());

			return ErrorCodes.ERROR_NOT_SUPPORTED;
		}

		return ErrorCodes.SUCCESS;
	}

	public int formatNdef(int nativeHandle, byte[] key) throws RemoteException {
		Log.e(TAG, "Attempt to call unsupported NDEF operation: formatNdef");

		return ErrorCodes.ERROR_IO;
	}

	public boolean getExtendedLengthApdusSupported() throws RemoteException {
		// Log.d(TAG, "getExtendedLengthApdusSupported");

		if (readerTechnology == null) {

			throw new RemoteException(NO_READER_MSG);
		}

		return readerTechnology.getExtendedLengthApdusSupported();
	}

	public int getMaxTransceiveLength(int technology) throws RemoteException {
		// Log.d(TAG, "getMaxTransceiveLength");

		if (readerTechnology == null) {
			throw new RemoteException(NO_READER_MSG);
		}

		return readerTechnology.getMaxTransceiveLength(technology);
	}

	public int[] getTechList(int nativeHandle) throws RemoteException {
		// Log.d(TAG, "getTechList");

		TagProxy proxy = store.get(nativeHandle);

		if (proxy != null) {
			List<TagTechnology> technologies = proxy.getTechnologies();

			int[] techList = new int[technologies.size()];

			for (int i = 0; i < techList.length; i++) {
				techList[i] = technologies.get(i).getTagTechnology();
			}

			return techList;
		}

		throw new RemoteException("No proxy for " + nativeHandle);
	}

	public int getTimeout(int technology) throws RemoteException {
		// Log.d(TAG, "getTimeout");

		if (readerTechnology == null) {
			throw new RemoteException(NO_READER_MSG);
		}

		return readerTechnology.getTimeout(technology);
	}

	public boolean isNdef(int nativeHandle) throws RemoteException {
		Log.e(TAG, "Attempt to call unsupported NDEF operation: isNdef");

		throw new RemoteException(UNSUPPORTED_OPERATION_MSG);
	}

	public boolean isPresent(int nativeHandle) throws RemoteException {
		// Log.d(TAG, "isPresent");
		TagProxy proxy = store.get(nativeHandle);

		if (proxy == null) {
			throw new RemoteException();
		}

		return proxy.isPresent();
	}

	public boolean ndefIsWritable(int nativeHandle) throws RemoteException {
		Log.e(TAG, "Attempt to call unsupported NDEF operation: ndefIsWritable");

		throw new RemoteException(UNSUPPORTED_OPERATION_MSG);
	}

	public int ndefMakeReadOnly(int nativeHandle) throws RemoteException {
		Log.e(TAG, "Attempt to call unsupported NDEF operation: ndefMakeReadOnly");

		throw new RemoteException(UNSUPPORTED_OPERATION_MSG);
	}

	public NdefMessage ndefRead(int nativeHandle) throws RemoteException {
		Log.e(TAG, "Attempt to call unsupported NDEF operation: ndefRead");

		throw new RemoteException(UNSUPPORTED_OPERATION_MSG);
	}

	public int ndefWrite(int nativeHandle, NdefMessage msg) throws RemoteException {
		Log.e(TAG, "Attempt to call unsupported NDEF operation: ndefWrite");

		throw new RemoteException(UNSUPPORTED_OPERATION_MSG);
	}

	public int reconnect(int nativehandle) throws RemoteException {
		// Log.d(TAG, "reconnect");

		if (readerTechnology == null) {
			throw new RemoteException(NO_READER_MSG);
		}

		return readerTechnology.reconnect(nativehandle);
	}

	public TagImpl rediscover(int nativehandle) throws RemoteException {
		// Log.d(TAG, "rediscover");

		TagProxy proxy = store.get(nativehandle);

		if (proxy == null) {
			throw new RemoteException();
		}

		return proxy.rediscover(this);
	}

	public void resetTimeouts() throws RemoteException {
		// Log.d(TAG, "resetTimeouts");

		if (readerTechnology == null) {
			throw new RemoteException(NO_READER_MSG);
		}

		readerTechnology.resetTimeouts();
	}

	public int setTimeout(int technology, int timeout) throws RemoteException {
		// Log.d(TAG, "setTimeout");

		if (readerTechnology == null) {
			throw new RemoteException(NO_READER_MSG);
		}

		return readerTechnology.setTimeout(technology, timeout);
	}

	public TransceiveResult transceive(int nativeHandle, byte[] data, boolean raw) throws RemoteException {
		// Log.d(TAG, "transceive");
		TagTechnology adapter = getConnected(nativeHandle);
		if (adapter != null) {
			if (adapter instanceof CommandTechnology) {
				CommandTechnology technology = (CommandTechnology) adapter;

				return technology.transceive(data, raw);
			} else {
				throw new RemoteException("Tag technology " + adapter.getClass().getName() + " does not support transceive(..)");
			}
		}

		return new TransceiveResult(TransceiveResult.RESULT_TAGLOST, null);
	}

	private byte[] noReaderException() {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream(out);

			dout.writeUTF("Reader not connected");

			byte[] response = out.toByteArray();

			return response;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
