package no.entur.android.nfc.wrapper.tech.utils.lock;

import java.io.IOException;

import no.entur.android.nfc.wrapper.Tag;
import no.entur.android.nfc.wrapper.tech.IsoDep;

/**
 *
 * Helper class for debugging multi-threading issues.
 *
 */

public class ThreadLockIsoDep extends IsoDep {

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private IsoDep isoDep;

        private boolean lockOnSetTimeout = true;
        private boolean lockOnTransceive = true;
        private boolean lockOnConnect = true;

        private ThreadLock lock;

        public Builder withLock(ThreadLock lock) {
            this.lock = lock;
            return this;
        }

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

        public ThreadLockIsoDep build() {
            if(isoDep == null) {
                throw new IllegalStateException();
            }
            if(lock == null) {
                throw new IllegalStateException();
            }
            return new ThreadLockIsoDep(isoDep, lock);
        }

    }

    private final IsoDep delegate;

    private final ThreadLock lock;

    public ThreadLockIsoDep(IsoDep delegate, ThreadLock lock) {
        this.delegate = delegate;
        this.lock = lock;
    }


    @Override
    public void setTimeout(int timeout) {
        if(lock.isLockOnSetTimeout()) {
            lock.lock();
        } else {
            lock.verifyLock();
        }
        delegate.setTimeout(timeout);
    }

    @Override
    public int getTimeout() {
        lock.verifyLock();
        return delegate.getTimeout();
    }

    @Override
    public byte[] getHistoricalBytes() {
        lock.verifyLock();
        return delegate.getHistoricalBytes();
    }

    @Override
    public byte[] getHiLayerResponse() {
        lock.verifyLock();
        return delegate.getHiLayerResponse();
    }

    @Override
    public byte[] transceive(byte[] data) throws IOException {
        if(lock.isLockOnTransceive()) {
            lock.lock();
        } else {
            lock.verifyLock();

        }
        return delegate.transceive(data);
    }

    @Override
    public int getMaxTransceiveLength() {
        lock.verifyLock();
        return delegate.getMaxTransceiveLength();
    }

    @Override
    public boolean isExtendedLengthApduSupported() {
        lock.verifyLock();
        return delegate.isExtendedLengthApduSupported();
    }

    @Override
    public boolean isNative() {
        lock.verifyLock();
        return delegate.isNative();
    }

    @Override
    public Tag getTag() {
        return delegate.getTag();
    }

    @Override
    public void connect() throws IOException {
        if(lock.isLockOnConnect()) {
            lock.lock();
        } else {
            lock.verifyLock();

        }
        delegate.connect();
    }

    @Override
    public void close() throws IOException {
        lock.verifyLock();
        delegate.close();
    }

    @Override
    public boolean isConnected() {
        lock.verifyLock();
        return delegate.isConnected();
    }

}
