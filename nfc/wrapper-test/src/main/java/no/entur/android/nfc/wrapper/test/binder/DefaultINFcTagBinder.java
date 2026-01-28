package no.entur.android.nfc.wrapper.test.binder;

import android.nfc.NdefMessage;
import android.os.RemoteException;

import no.entur.android.nfc.wrapper.INfcTag;
import no.entur.android.nfc.wrapper.ParcelableTransceive;
import no.entur.android.nfc.wrapper.ParcelableTransceiveMetadata;
import no.entur.android.nfc.wrapper.ParcelableTransceiveMetadataResult;
import no.entur.android.nfc.wrapper.ParcelableTransceiveResult;
import no.entur.android.nfc.wrapper.TagImpl;
import no.entur.android.nfc.wrapper.TransceiveResult;

public class DefaultINFcTagBinder extends INfcTag.Stub {

    private static int counter = 1;

    public static int nextServiceHandle() {
        synchronized (DefaultINFcTagBinder.class) {
            counter++;

            return counter;
        }
    }

    protected final int serviceHandle;
    protected INFcTagBinder delegate;
    protected boolean lost;

	public DefaultINFcTagBinder() {
        serviceHandle = nextServiceHandle();
	}

    public void setDelegate(INFcTagBinder delegate) {
        this.delegate = delegate;
    }

    public INFcTagBinder get(int nativeHandle) throws RemoteException {
        if(nativeHandle != serviceHandle) {
            throw new RemoteException("Expected handle " + serviceHandle + ", got " + nativeHandle);
        }
        if(lost) {
            throw new RemoteException("Test class closed");
        }
        return delegate;
	}

    public void present() {
        this.lost = false;
    }

    public void lost() {
        this.lost = true;
    }

    @Override
    public int connect(int nativeHandle, int technology) throws RemoteException {
        return get(nativeHandle).connect(technology);
    }

    @Override
    public int reconnect(int nativeHandle) throws RemoteException {
        return get(nativeHandle).reconnect();
    }

    @Override
    public int[] getTechList(int nativeHandle) throws RemoteException {
        return get(nativeHandle).getTechList();
    }

    @Override
    public boolean isNdef(int nativeHandle) throws RemoteException {
        return get(nativeHandle).isNdef();
    }

    @Override
    public boolean isPresent(int nativeHandle) throws RemoteException {
        return !lost;
    }

    @Override
    public TransceiveResult transceive(int nativeHandle, byte[] data, boolean raw) throws RemoteException {
        return get(nativeHandle).transceive(data, raw);
    }

    @Override
    public NdefMessage ndefRead(int nativeHandle) throws RemoteException {
        return get(nativeHandle).ndefRead();
    }

    @Override
    public int ndefWrite(int nativeHandle, NdefMessage msg) throws RemoteException {
        return get(nativeHandle).ndefWrite(msg);
    }

    @Override
    public int ndefMakeReadOnly(int nativeHandle) throws RemoteException {
        return get(nativeHandle).ndefMakeReadOnly();
    }

    @Override
    public boolean ndefIsWritable(int nativeHandle) throws RemoteException {
        return get(nativeHandle).ndefIsWritable();
    }

    @Override
    public int formatNdef(int nativeHandle, byte[] key) throws RemoteException {
        return get(nativeHandle).formatNdef(key);
    }

    @Override
    public TagImpl rediscover(int nativeHandle) throws RemoteException {
        return get(nativeHandle).rediscover();
    }

    @Override
    public int setTimeout(int technology, int timeout) throws RemoteException {
        return delegate.setTimeout(technology, timeout);
    }

    @Override
    public int getTimeout(int technology) throws RemoteException {
        return delegate.getTimeout(technology);
    }

    @Override
    public void resetTimeouts() throws RemoteException {
        delegate.resetTimeouts();
    }

    @Override
    public boolean canMakeReadOnly(int ndefType) throws RemoteException {
        return delegate.canMakeReadOnly(ndefType);
    }

    @Override
    public int getMaxTransceiveLength(int technology) throws RemoteException {
        return delegate.getMaxTransceiveLength(technology);
    }

    @Override
    public boolean getExtendedLengthApdusSupported() throws RemoteException {
        return delegate.getExtendedLengthApdusSupported();
    }

    @Override
    public ParcelableTransceiveResult parcelableTranscieve(int nativeHandle, ParcelableTransceive data) throws RemoteException {
        return delegate.parcelableTranscieve(data);
    }

    @Override
    public ParcelableTransceiveMetadataResult parcelableTransceiveMetadata(ParcelableTransceiveMetadata data) throws RemoteException {
        return delegate.parcelableTransceiveMetadata(data);
    }

    public boolean isLost() {
        return lost;
    }

    public int getServiceHandle() {
        return serviceHandle;
    }
}
