package no.entur.android.nfc.wrapper.tech.utils.bulk;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.os.Parcel;

import java.util.LinkedList;

/**
 * Mocked {@link Parcel} which hands back written values in FIFO order, so that write / read
 * mismatches surface as exceptions or leftover values in plain JVM unit tests.
 */
public class FakeParcel {

    private final LinkedList<Object> values = new LinkedList<>();

    private final Parcel parcel = mock(Parcel.class);

    public FakeParcel() {
        doAnswer(i -> values.add(i.getArgument(0))).when(parcel).writeInt(anyInt());
        doAnswer(i -> values.add(i.getArgument(0))).when(parcel).writeString(any());

        when(parcel.readInt()).thenAnswer(i -> (Integer) values.remove());
        when(parcel.readString()).thenAnswer(i -> (String) values.remove());
    }

    public Parcel getParcel() {
        return parcel;
    }

    public int remaining() {
        return values.size();
    }
}
