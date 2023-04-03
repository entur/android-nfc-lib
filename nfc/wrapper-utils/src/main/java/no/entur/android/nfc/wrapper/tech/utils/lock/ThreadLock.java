package no.entur.android.nfc.wrapper.tech.utils.lock;

import android.os.Parcelable;

import java.io.PrintWriter;
import java.io.StringWriter;

import no.entur.android.nfc.wrapper.tech.IsoDep;

public class ThreadLock {


    public static class Builder {

        private boolean lockOnSetTimeout = true;
        private boolean lockOnTransceive = true;
        private boolean lockOnConnect = true;

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

        public ThreadLock build() {
            return new ThreadLock(lockOnSetTimeout, lockOnTransceive, lockOnConnect);
        }

    }
    private StackTraceElement[] stackTrace;
    private Thread thread;

    private final boolean lockOnSetTimeout;
    private final boolean lockOnTransceive;
    private final boolean lockOnConnect;


    public ThreadLock(boolean lockOnSetTimeout, boolean lockOnTransceive, boolean lockOnConnect) {
        this.lockOnSetTimeout = lockOnSetTimeout;
        this.lockOnTransceive = lockOnTransceive;
        this.lockOnConnect = lockOnConnect;
    }
    
    public boolean isLocked() {
        return thread != null;
    }

    public void lock() {
        synchronized (this) {
            Thread currentThread = Thread.currentThread();
            if(this.thread != null) {
                if(this.thread != currentThread) {
                    throw new IllegalStateException("Thread " + currentThread + " cannot lock, already locked by " + this.thread + " using " + this.printStackTrace());
                }
            } else {
                this.thread = Thread.currentThread();
                this.stackTrace = Thread.currentThread().getStackTrace();
            }
        }
    }

    public void verifyLock() {
        synchronized (this) {
            if(this.thread != null) {
                Thread currentThread = Thread.currentThread();
                if(this.thread != currentThread) {
                    throw new IllegalStateException("Thread " + currentThread + " cannot lock, already locked by " + this.thread + " using " + this.printStackTrace());
                }
            }
        }
    }

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

    public boolean isLockOnSetTimeout() {
        return lockOnSetTimeout;
    }

    public boolean isLockOnTransceive() {
        return lockOnTransceive;
    }

    public boolean isLockOnConnect() {
        return lockOnConnect;
    }
}
