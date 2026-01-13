package no.entur.android.nfc.wrapper.tech.utils;

import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class TagTechnologyInvocationRecorder implements TagTechnologyInvocationListener {

    private List<TagTechnologyInvocation> invocations = new ArrayList<>(64);

    @Override
    public ConnectInvocation onConnect() {
        ConnectInvocation invocation = new ConnectInvocation();
        invocation.setTimestamp(System.nanoTime());
        this.invocations.add(invocation);
        return invocation;
    }

    @Override
    public CloseInvocation onClose() {
        CloseInvocation invocation = new CloseInvocation();
        invocation.setTimestamp(System.nanoTime());
        this.invocations.add(invocation);
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
    public ParcelableTransceiveInvocation onParcelableTransceive(Parcelable command) {
        ParcelableTransceiveInvocation transceive = new ParcelableTransceiveInvocation();
        transceive.setCommand(command);
        transceive.setTimestamp(System.nanoTime());
        this.invocations.add(transceive);
        return transceive;
    }

    @Override
    public SetTimeoutInvocation onSetTimeout(int value) {
        SetTimeoutInvocation invocation = new SetTimeoutInvocation();
        invocation.setTimestamp(System.nanoTime());
        invocation.setValue(value);
        this.invocations.add(invocation);
        return invocation;
    }

    public List<TagTechnologyInvocation> getInvocations() {
        return invocations;
    }
}
