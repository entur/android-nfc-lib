package no.entur.android.nfc.wrapper.tech.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import no.entur.android.nfc.wrapper.Tag;
import no.entur.android.nfc.wrapper.TagImpl;
import no.entur.android.nfc.wrapper.tech.IsoDep;

/**
 *
 * Helper class for debugging multi-threading issues.
 *
 */

public class LockThreadIsoDep extends IsoDep {

    private static class Lock {
        public StackTraceElement[] stackTrace;
        private Thread thread;
        private String methodName;

        public String printStackTrace() {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);

            pw.println();
            for (int i = 1; i < stackTrace.length; i++) {
                pw.println("\t > " + stackTrace[i]);
            }
            pw.flush();

            return sw.toString();
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private IsoDep isoDep;

        private boolean lockOnSetTimeout = true;
        private boolean lockOnTransceive = true;
        private boolean lockOnConnect = true;

        public Builder withIsoDep(IsoDep isoDep) {
            this.isoDep = isoDep;
            return this;
        }

        public Builder withLockOnSetTimeout(boolean enabled) {
            this.lockOnSetTimeout = enabled;
            return this;
        }

        public Builder withLockOnTransceive(boolean enabled) {
            this.lockOnTransceive = enabled;
            return this;
        }

        public Builder withLockOnConnect(boolean enabled) {
            this.lockOnConnect = enabled;
            return this;
        }

        public LockThreadIsoDep build() {
            if(isoDep == null) {
                throw new IllegalStateException();
            }
            return new LockThreadIsoDep(isoDep, lockOnSetTimeout, lockOnTransceive, lockOnConnect);
        }

    }

    private final IsoDep delegate;
    private final boolean lockOnSetTimeout;
    private final boolean lockOnTransceive;
    private final boolean lockOnConnect;

    private volatile Lock lock;

    public LockThreadIsoDep(IsoDep delegate, boolean lockOnSetTimeout, boolean lockOnTransceive, boolean lockOnConnect) {
        this.delegate = delegate;
        this.lockOnSetTimeout = lockOnSetTimeout;
        this.lockOnTransceive = lockOnTransceive;
        this.lockOnConnect = lockOnConnect;
    }

    public void lock(String method) {
        synchronized (this) {
            Thread currentThread = Thread.currentThread();
            if(this.lock != null) {
                if(this.lock.thread != currentThread) {
                    throw new IllegalStateException("Thread " + currentThread + " cannot lock via method " + method +"(..), already locked by " + this.lock.thread + " using method " + this.lock.methodName + "(..) via " + this.lock.printStackTrace());
                }
            } else {
                Lock lock = new Lock();
                lock.thread = currentThread;
                lock.methodName = method;
                lock.stackTrace = Thread.currentThread().getStackTrace();
                this.lock = lock;
            }
        }
    }

    public void verifyLock(String method) {
        synchronized (this) {
            if(this.lock != null) {
                Thread currentThread = Thread.currentThread();
                if(this.lock.thread != currentThread) {
                    throw new IllegalStateException("Thread " + currentThread + " cannot invoke method " + method +"(..), locked by " + this.lock.thread + " using method " + this.lock.methodName + "(..) via " + this.lock.printStackTrace());
                }
            }
        }
    }

    @Override
    public void setTimeout(int timeout) {
        if(lockOnSetTimeout) {
            lock("setTimeout");
        } else {
            verifyLock("setTimeout");
        }
        delegate.setTimeout(timeout);
    }

    @Override
    public int getTimeout() {
        verifyLock("getTimeout");
        return delegate.getTimeout();
    }

    @Override
    public byte[] getHistoricalBytes() {
        verifyLock("getHistoricalBytes");
        return delegate.getHistoricalBytes();
    }

    @Override
    public byte[] getHiLayerResponse() {
        verifyLock("getHiLayerResponse");
        return delegate.getHiLayerResponse();
    }

    @Override
    public byte[] transceive(byte[] data) throws IOException {
        if(lockOnTransceive) {
            lock("transceive");
        } else {
            verifyLock("transceive");
        }
        return delegate.transceive(data);
    }

    @Override
    public int getMaxTransceiveLength() {
        verifyLock("getMaxTransceiveLength");
        return delegate.getMaxTransceiveLength();
    }

    @Override
    public boolean isExtendedLengthApduSupported() {
        verifyLock("isExtendedLengthApduSupported");
        return delegate.isExtendedLengthApduSupported();
    }

    @Override
    public boolean isNative() {
        verifyLock("isNative");
        return delegate.isNative();
    }

    @Override
    public Tag getTag() {
        return delegate.getTag();
    }

    @Override
    public void connect() throws IOException {
        if(lockOnConnect) {
            lock("connect");
        } else {
            verifyLock("connect");
        }
        delegate.connect();
    }

    @Override
    public void close() throws IOException {
        verifyLock("close");
        delegate.close();
    }

    @Override
    public boolean isConnected() {
        verifyLock("isConnected");
        return delegate.isConnected();
    }

}
