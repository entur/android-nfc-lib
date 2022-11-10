package no.entur.android.nfc.wrapper.tech.utils;

public class CloseInvocation extends AbstractTagTechnologyInvocation {

    private boolean returned = false;
    private Exception exception;

    public boolean isReturned() {
        return returned;
    }

    public void setReturned(boolean returned) {
        this.returned = returned;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    @Override
    public void done(long timestamp) {
        this.returned = true;

        super.done(timestamp);
    }

}
