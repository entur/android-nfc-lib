package no.entur.android.nfc.wrapper.tech.utils;

import android.os.Parcelable;

public class ParcelableTransceiveInvocation extends AbstractTagTechnologyInvocation implements TagTechnologyInvocation {

    protected Parcelable command;
    protected Parcelable response;

    public Parcelable getCommand() {
        return command;
    }

    public void setCommand(Parcelable command) {
        this.command = command;
    }

    public Parcelable getResponse() {
        return response;
    }

    public void setResponse(Parcelable response) {
        this.response = response;
    }

}
