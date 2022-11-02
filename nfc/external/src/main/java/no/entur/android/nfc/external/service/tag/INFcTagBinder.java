package no.entur.android.nfc.external.service.tag;

import android.nfc.NdefMessage;
import android.os.RemoteException;
import android.util.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.List;

import no.entur.android.nfc.external.tag.DESFireAdapter;
import no.entur.android.nfc.wrapper.ErrorCodes;
import no.entur.android.nfc.wrapper.INfcTag;
import no.entur.android.nfc.wrapper.TagImpl;
import no.entur.android.nfc.wrapper.TransceiveResult;
import no.entur.android.nfc.wrapper.tech.Ndef;
import no.entur.android.nfc.wrapper.tech.NdefFormatable;

public class INFcTagBinder extends INfcTag.Stub {

	private static final Logger LOGGER = LoggerFactory.getLogger(INFcTagBinder.class);

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
		// LOGGER.debug("canMakeReadOnly");

		return readerTechnology.canMakeReadOnly(ndefType);
	}

	public int close(int nativeHandle) throws RemoteException {
		// LOGGER.debug("close");

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
		// LOGGER.debug("connect");

		TagProxy proxy = store.get(serviceHandle);

		if (proxy == null) {
			LOGGER.debug("No proxy for " + serviceHandle);

			return ErrorCodes.ERROR_CONNECT;
		}

		if (!proxy.selectTechnology(technology)) {
			LOGGER.debug("No technology " + technology + " for " + serviceHandle + ": " + proxy.getTechnologies());

			return ErrorCodes.ERROR_NOT_SUPPORTED;
		}

		return ErrorCodes.SUCCESS;
	}

	public int formatNdef(int nativeHandle, byte[] key) throws RemoteException {
		TagTechnology adapter = getConnected(nativeHandle);
		if (adapter != null) {
			if (adapter instanceof NdefFormattableTechnology) {
				NdefFormattableTechnology technology = (NdefFormattableTechnology) adapter;

				return technology.formatNdef();
			} else {
				throw new RemoteException("Tag technology " + adapter.getClass().getName() + " does not support formatNdef(..)");
			}
		}
		throw new RemoteException("No connected tag technology");
	}

	public boolean getExtendedLengthApdusSupported() throws RemoteException {
		// LOGGER.debug("getExtendedLengthApdusSupported");

		if (readerTechnology == null) {

			throw new RemoteException(NO_READER_MSG);
		}

		return readerTechnology.getExtendedLengthApdusSupported();
	}

	public int getMaxTransceiveLength(int technology) throws RemoteException {
		// LOGGER.debug("getMaxTransceiveLength");

		if (readerTechnology == null) {
			throw new RemoteException(NO_READER_MSG);
		}

		return readerTechnology.getMaxTransceiveLength(technology);
	}

	public int[] getTechList(int nativeHandle) throws RemoteException {
		// LOGGER.debug("getTechList");

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
		// LOGGER.debug("getTimeout");

		if (readerTechnology == null) {
			throw new RemoteException(NO_READER_MSG);
		}

		return readerTechnology.getTimeout(technology);
	}

	public boolean isNdef(int nativeHandle) throws RemoteException {
		TagProxy proxy = store.get(nativeHandle);

		if (proxy == null) {
			throw new RemoteException();
		}

		return proxy.isNdef();
	}

	public boolean isPresent(int nativeHandle) throws RemoteException {
		// LOGGER.debug("isPresent");
		TagProxy proxy = store.get(nativeHandle);

		if (proxy == null) {
			throw new RemoteException();
		}

		return proxy.isPresent();
	}

	public boolean ndefIsWritable(int nativeHandle) throws RemoteException {
		TagProxy proxy = store.get(nativeHandle);

		if (proxy == null) {
			throw new RemoteException();
		}

		return proxy.ndefIsWritable();
	}

	public int ndefMakeReadOnly(int nativeHandle) throws RemoteException {
		TagTechnology adapter = getConnected(nativeHandle);
		if (adapter != null) {
			if (adapter instanceof NdefTechnology) {
				NdefTechnology technology = (NdefTechnology) adapter;

				return technology.ndefMakeReadOnly();
			} else {
				throw new RemoteException("Tag technology " + adapter.getClass().getName() + " does not support transceive(..)");
			}
		}
		throw new RemoteException("No connected tag technology");
	}

	public NdefMessage ndefRead(int nativeHandle) throws RemoteException {
		TagTechnology adapter = getConnected(nativeHandle);
		if (adapter != null) {
			if (adapter instanceof NdefTechnology) {
				NdefTechnology technology = (NdefTechnology) adapter;

				return technology.ndefRead();
			} else {
				throw new RemoteException("Tag technology " + adapter.getClass().getName() + " does not support transceive(..)");
			}
		}
		throw new RemoteException("No connected tag technology");
	}

	public int ndefWrite(int nativeHandle, NdefMessage msg) throws RemoteException {
		TagTechnology adapter = getConnected(nativeHandle);
		if (adapter != null) {
			if (adapter instanceof NdefTechnology) {
				NdefTechnology technology = (NdefTechnology) adapter;

				return technology.ndefWrite(msg);
			} else {
				throw new RemoteException("Tag technology " + adapter.getClass().getName() + " does not support transceive(..)");
			}
		}
		throw new RemoteException("No connected tag technology");
	}

	public int reconnect(int nativehandle) throws RemoteException {
		// LOGGER.debug("reconnect");

		if (readerTechnology == null) {
			throw new RemoteException(NO_READER_MSG);
		}

		return readerTechnology.reconnect(nativehandle);
	}

	public TagImpl rediscover(int nativehandle) throws RemoteException {
		// LOGGER.debug("rediscover");

		TagProxy proxy = store.get(nativehandle);

		if (proxy == null) {
			throw new RemoteException();
		}

		return proxy.rediscover(this);
	}

	public void resetTimeouts() throws RemoteException {
		// LOGGER.debug("resetTimeouts");

		if (readerTechnology == null) {
			throw new RemoteException(NO_READER_MSG);
		}

		readerTechnology.resetTimeouts();
	}

	public int setTimeout(int technology, int timeout) throws RemoteException {
		// LOGGER.debug("setTimeout");

		if (readerTechnology == null) {
			throw new RemoteException(NO_READER_MSG);
		}

		return readerTechnology.setTimeout(technology, timeout);
	}

	public TransceiveResult transceive(int nativeHandle, byte[] data, boolean raw) throws RemoteException {
		// LOGGER.debug("transceive");
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
