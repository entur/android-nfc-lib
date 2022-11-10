package no.entur.android.nfc.wrapper.tech.utils;

public class SetTimeoutInvocation extends AbstractTagTechnologyInvocation {

    private int value;
    private boolean returned;

    public void setReturned(boolean returned) {
        this.returned = returned;
    }

    public boolean isReturned() {
        return returned;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public void done(long timestamp) {
        this.returned = true;

        super.done(timestamp);
    }
}
