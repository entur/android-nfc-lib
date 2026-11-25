package no.entur.android.nfc.wrapper.test.binder;

import static org.junit.Assert.fail;

import android.os.RemoteException;

import org.junit.Test;

public class DefaultINFcTagBinderTest {

    @Test
    public void testTransceiveMetadataWhenLost() throws Exception {
        DefaultINFcTagBinder binder = new DefaultINFcTagBinder();
        // no delegate needed, the lost check comes first
        binder.lost();

        try {
            binder.parcelableTransceiveMetadata(null);
            fail();
        } catch (RemoteException e) {
            // expected
        }
    }
}
