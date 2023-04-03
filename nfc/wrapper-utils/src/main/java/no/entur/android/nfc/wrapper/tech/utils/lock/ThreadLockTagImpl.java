package no.entur.android.nfc.wrapper.tech.utils.lock;

import android.os.Parcel;

import no.entur.android.nfc.wrapper.Tag;

public class ThreadLockTagImpl extends Tag {

    private final Tag delegate;
    private final ThreadLock lock;

    public ThreadLockTagImpl(Tag delegate, ThreadLock lock) {
        this.delegate = delegate;
        this.lock = lock;
    }

    public byte[] getId() {
        return delegate.getId();
    }

    public String[] getTechList() {
        return delegate.getTechList();
    }

    public Tag getDelegate() {
        return delegate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        throw new IllegalArgumentException("Cannot write to parcel");
    }

    public static final Creator<ThreadLockTagImpl> CREATOR = new Creator<ThreadLockTagImpl>() {
        @Override
        public ThreadLockTagImpl createFromParcel(Parcel source) {
            throw new IllegalArgumentException("Cannot create from parcel");
        }

        @Override
        public ThreadLockTagImpl[] newArray(int size) {
            return new ThreadLockTagImpl[size];
        }
    };
}
