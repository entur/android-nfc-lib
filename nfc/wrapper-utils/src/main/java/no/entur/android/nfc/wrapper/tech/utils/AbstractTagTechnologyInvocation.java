package no.entur.android.nfc.wrapper.tech.utils;

public class AbstractTagTechnologyInvocation implements TagTechnologyInvocation {

    protected long timestamp;
    protected long duration;
    protected Exception exception;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void done(long timestamp) {
        this.duration = timestamp - this.timestamp;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public Exception getException() {
        return exception;
    }
}
