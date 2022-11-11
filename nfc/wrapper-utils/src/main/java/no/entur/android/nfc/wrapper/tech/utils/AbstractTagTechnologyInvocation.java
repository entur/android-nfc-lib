package no.entur.android.nfc.wrapper.tech.utils;

public class AbstractTagTechnologyInvocation implements TagTechnologyInvocation {

    protected long timestamp;
    protected long duration;
    protected Exception exception;
    protected boolean completed;

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isCompleted() {
        return completed;
    }

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

    public void completed(long timestamp) {
        this.completed = true;
        this.duration = timestamp - this.timestamp;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public Exception getException() {
        return exception;
    }


}
