package no.entur.android.nfc.external.acs.reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import no.entur.android.nfc.external.acs.reader.bind.IAcr122UReaderControl;

@RunWith(AndroidJUnit4.class)
public class Acr122UReaderTest {

    @Test
    public void testParcel() {
        IBinder binder = new Binder();
        IAcr122UReaderControl readerControl = IAcr122UReaderControl.Stub.asInterface(binder);

        Acr122UReader reader = new Acr122UReader("ACR122U", "acs-1", readerControl);

        Parcel parcel = Parcel.obtain();
        try {
            reader.writeToParcel(parcel, 0);
            parcel.setDataPosition(0);

            Acr122UReader result = Acr122UReader.CREATOR.createFromParcel(parcel);

            assertEquals("ACR122U", result.getName());
            assertEquals("acs-1", result.getId());
            assertSame(binder, result.readerControl.asBinder());
            assertEquals(0, parcel.dataAvail());
        } finally {
            parcel.recycle();
        }
    }
}
