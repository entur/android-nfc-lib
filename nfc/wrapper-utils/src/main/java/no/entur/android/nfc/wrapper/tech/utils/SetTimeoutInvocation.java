package no.entur.android.nfc.wrapper.tech.utils;

public class SetTimeoutInvocation extends AbstractTagTechnologyInvocation {

    private int value;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public void completed(long timestamp) {
        this.completed = true;

        super.completed(timestamp);
    }
}
