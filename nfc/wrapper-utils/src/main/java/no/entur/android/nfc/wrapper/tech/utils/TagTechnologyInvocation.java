package no.entur.android.nfc.wrapper.tech.utils;

/**
 *
 * Interface for tracking interesting calls to tag technologies like IsoDep.
 *
 */

public interface TagTechnologyInvocation {

    long getTimestamp();
    void setTimestamp(long timestamp);

    long getDuration();
    void setDuration(long duration);

    void completed(long timestamp);
    void setException(Exception exception);

    boolean isCompleted();
    void setCompleted(boolean completed);

    Exception getException();
}
