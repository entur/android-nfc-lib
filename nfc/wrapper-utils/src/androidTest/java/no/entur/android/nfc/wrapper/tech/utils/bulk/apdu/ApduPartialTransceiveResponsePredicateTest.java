package no.entur.android.nfc.wrapper.tech.utils.bulk.apdu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.os.Parcel;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ApduPartialTransceiveResponsePredicateTest {

    @Test
    public void testParcelCustomStatus() {
        ApduPartialTransceiveResponsePredicate result = roundTrip(new ApduPartialTransceiveResponsePredicate(0x61, 0x00));

        assertEquals(0x61, result.getSw1());
        assertEquals(0x00, result.getSw2());

        assertTrue(result.test(new byte[]{0x01, 0x61, 0x00}));
        assertFalse(result.test(new byte[]{0x01, (byte) 0x91, (byte) 0xAF}));
    }

    @Test
    public void testParcelDefaultStatus() {
        ApduPartialTransceiveResponsePredicate result = roundTrip(new ApduPartialTransceiveResponsePredicate());

        assertEquals(0x91, result.getSw1());
        assertEquals(0xAF, result.getSw2());
    }

    private ApduPartialTransceiveResponsePredicate roundTrip(ApduPartialTransceiveResponsePredicate predicate) {
        Parcel parcel = Parcel.obtain();
        try {
            predicate.writeToParcel(parcel, 0);
            parcel.setDataPosition(0);

            ApduPartialTransceiveResponsePredicate result = ApduPartialTransceiveResponsePredicate.CREATOR.createFromParcel(parcel);

            assertEquals(0, parcel.dataAvail());

            return result;
        } finally {
            parcel.recycle();
        }
    }
}
