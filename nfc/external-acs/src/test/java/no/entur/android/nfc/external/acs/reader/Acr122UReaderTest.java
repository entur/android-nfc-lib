package no.entur.android.nfc.external.acs.reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.os.IBinder;
import android.os.Parcel;

import org.junit.Test;

import java.util.LinkedList;

import no.entur.android.nfc.external.acs.reader.bind.IAcr122UReaderControl;

public class Acr122UReaderTest {

    @Test
    public void testParcel() {
        IBinder binder = mock(IBinder.class);
        IAcr122UReaderControl readerControl = mock(IAcr122UReaderControl.class);
        when(readerControl.asBinder()).thenReturn(binder);
        when(binder.queryLocalInterface(anyString())).thenReturn(readerControl);

        Acr122UReader reader = new Acr122UReader("ACR122U", "acs-1", readerControl);

        // mocked parcel which hands back written values in FIFO order
        LinkedList<Object> values = new LinkedList<>();
        Parcel parcel = mock(Parcel.class);
        doAnswer(i -> values.add(i.getArgument(0))).when(parcel).writeString(any());
        doAnswer(i -> values.add(i.getArgument(0))).when(parcel).writeStrongBinder(any());
        when(parcel.readString()).thenAnswer(i -> (String) values.remove());
        when(parcel.readStrongBinder()).thenAnswer(i -> (IBinder) values.remove());

        reader.writeToParcel(parcel, 0);

        Acr122UReader result = Acr122UReader.CREATOR.createFromParcel(parcel);

        assertEquals("ACR122U", result.getName());
        assertEquals("acs-1", result.getId());
        assertSame(readerControl, result.readerControl);
        assertEquals(0, values.size());
    }
}
