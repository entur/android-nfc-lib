package no.entur.android.nfc.external.service.tag;

import static org.junit.Assert.assertThrows;

import android.os.RemoteException;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class INFcTagBinderTest {

    @Test
    public void testCanMakeReadOnlyWithoutReader() {
        INFcTagBinder binder = new INFcTagBinder(new DefaultTagProxyStore());

        assertThrows(RemoteException.class, () -> binder.canMakeReadOnly(1));
    }
}
