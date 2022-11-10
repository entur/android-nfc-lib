package no.entur.android.nfc.wrapper.tech.utils;

public class TransceiveInvocation extends AbstractTagTechnologyInvocation implements TagTechnologyInvocation {

    protected byte[] command;
    protected byte[] response;

    public byte[] getCommand() {
        return command;
    }

    public void setCommand(byte[] command) {
        this.command = command;
    }

    public byte[] getResponse() {
        return response;
    }

    public void setResponse(byte[] response) {
        this.response = response;
    }

}
