package no.entur.android.nfc.wrapper.tech.utils;

import java.util.ArrayList;
import java.util.List;

public class TagTechnologyInvocationRecorder implements TagTechnologyInvocationListener {

    private List<TagTechnologyInvocation> invocations = new ArrayList<>(64);

    @Override
    public ConnectInvocation onConnect() {
        ConnectInvocation connectInvocation = new ConnectInvocation();
        connectInvocation.setTimestamp(System.nanoTime());
        return connectInvocation;
    }

    @Override
    public CloseInvocation onClose() {
        CloseInvocation invocation = new CloseInvocation();
        invocation.setTimestamp(System.nanoTime());
        return invocation;
    }

    @Override
    public TransceiveInvocation onTransceive(byte[] command) {
        TransceiveInvocation transceive = new TransceiveInvocation();
        transceive.setCommand(command);
        transceive.setTimestamp(System.nanoTime());
        this.invocations.add(transceive);
        return transceive;
    }

    @Override
    public SetTimeoutInvocation onSetTimeout(int value) {
        SetTimeoutInvocation invocation = new SetTimeoutInvocation();
        invocation.setTimestamp(System.nanoTime());
        invocation.setCompleted(value);
        return invocation;
    }

    public List<TagTechnologyInvocation> getInvocations() {
        return invocations;
    }
}
