package no.entur.android.nfc.wrapper.tech.utils;

public class ConnectInvocation extends AbstractTagTechnologyInvocation {

    private boolean completed = false;
    private Exception exception;

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    @Override
    public void done(long timestamp) {
        this.completed = true;

        super.done(timestamp);
    }

}
