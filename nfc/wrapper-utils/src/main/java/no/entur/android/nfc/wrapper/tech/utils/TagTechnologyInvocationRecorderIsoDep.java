package no.entur.android.nfc.wrapper.tech.utils;

import java.io.IOException;
import java.util.List;

import no.entur.android.nfc.wrapper.Tag;
import no.entur.android.nfc.wrapper.TagImpl;
import no.entur.android.nfc.wrapper.tech.IsoDep;

/**
 *
 * Helper class for transparent recording of tag interactions.
 *
 */

public class TagTechnologyInvocationRecorderIsoDep extends IsoDep {

    /**
     * Get an instance of {@link IsoDep} for the given tag.
     * <p>
     * Does not cause any RF activity and does not block.
     * <p>
     * Returns null if {@link IsoDep} was not enumerated in {@link TagImpl#getTechList}. This indicates the tag does not support ISO-DEP.
     *
     * @param tag an ISO-DEP compatible tag
     * @return ISO-DEP object
     */
    public static TagTechnologyInvocationRecorderIsoDep get(Tag tag) {
        IsoDep isoDep = IsoDep.get(tag);
        if(isoDep != null) {
            return new TagTechnologyInvocationRecorderIsoDep(isoDep);
        }
        return null;
    }

    private final IsoDep delegate;
    private final TagTechnologyInvocationRecorder listener = new TagTechnologyInvocationRecorder();

    public TagTechnologyInvocationRecorderIsoDep(IsoDep delegate) {
        this.delegate = delegate;
    }

    @Override
    public void setTimeout(int timeout) {
        SetTimeoutInvocation transceiveInvocation = listener.onSetTimeout(timeout);
        try {
            delegate.setTimeout(timeout);
        } catch (Exception e) {
            transceiveInvocation.setException(e);
            throw e;
        } finally {
            transceiveInvocation.done(System.nanoTime());
        }
    }

    @Override
    public int getTimeout() {
        return delegate.getTimeout();
    }

    @Override
    public byte[] getHistoricalBytes() {
        return delegate.getHistoricalBytes();
    }

    @Override
    public byte[] getHiLayerResponse() {
        return delegate.getHiLayerResponse();
    }

    @Override
    public byte[] transceive(byte[] data) throws IOException {
        TransceiveInvocation invocation = listener.onTransceive(data);
        try {
            byte[] transceive = delegate.transceive(data);
            invocation.setResponse(transceive);
            return transceive;
        } catch (Exception e) {
            invocation.setException(e);
            throw e;
        } finally {
            invocation.done(System.nanoTime());
        }
    }

    @Override
    public int getMaxTransceiveLength() {
        return delegate.getMaxTransceiveLength();
    }

    @Override
    public boolean isExtendedLengthApduSupported() {
        return delegate.isExtendedLengthApduSupported();
    }

    @Override
    public Tag getTag() {
        return delegate.getTag();
    }

    @Override
    public void connect() throws IOException {
        ConnectInvocation invocation = listener.onConnect();
        try {
            delegate.connect();
        } catch (Exception e) {
            invocation.setException(e);
            throw e;
        } finally {
            invocation.done(System.nanoTime());
        }

    }

    @Override
    public void close() throws IOException {
        CloseInvocation invocation = listener.onClose();
        try {
            delegate.close();
        } catch (Exception e) {
            invocation.setException(e);
            throw e;
        } finally {
            invocation.done(System.nanoTime());
        }
    }

    @Override
    public boolean isConnected() {
        return delegate.isConnected();
    }

    public List<TagTechnologyInvocation> getInvocations() {
        return listener.getInvocations();
    }
}
