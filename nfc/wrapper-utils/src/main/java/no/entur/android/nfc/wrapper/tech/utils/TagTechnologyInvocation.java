package no.entur.android.nfc.wrapper.tech.utils;

/**
 *
 * Interface for tracking interesting calls to tag technologies like IsoDep.
 *
 */

interface TagTechnologyInvocation {

    long getTimestamp();
    void setTimestamp(long timestamp);

    long getDuration();
    void setDuration(long duration);

    void done(long timestamp);
    void setException(Exception exception);

    Exception getException();
}
