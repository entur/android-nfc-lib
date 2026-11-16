package no.entur.android.nfc.wrapper.tech.utils.bulk.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import android.os.Parcel;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

@RunWith(AndroidJUnit4.class)
public class CommandMetadataResponseTest {

    @Test
    public void testParcelWithBulkMetadata() {
        assertRoundTrip(new BulkTransceiveMetadata(false, true));
        assertRoundTrip(new BulkTransceiveMetadata(true, false));
        assertRoundTrip(new BulkTransceiveMetadata(false, false));
        assertRoundTrip(new BulkTransceiveMetadata(true, true));
    }

    @Test
    public void testParcelWithoutBulkMetadata() {
        CommandMetadataResponse response = new CommandMetadataResponse(Arrays.asList(CommandMetadataResponse.COMMAND_FORMAT_ISO7816), null);

        CommandMetadataResponse result = roundTrip(response);

        assertEquals(response.getFormats(), result.getFormats());
        assertNull(result.getBulkMetadata());
    }

    private void assertRoundTrip(BulkTransceiveMetadata metadata) {
        CommandMetadataResponse response = new CommandMetadataResponse(Arrays.asList(CommandMetadataResponse.COMMAND_FORMAT_ISO7816, CommandMetadataResponse.COMMAND_FORMAT_NATIVE_MIFARE_DESFIRE), metadata);

        CommandMetadataResponse result = roundTrip(response);

        assertEquals(response.getFormats(), result.getFormats());
        assertEquals(metadata.isResponsePredicate(), result.getBulkMetadata().isResponsePredicate());
        assertEquals(metadata.isPartialResponsePredicate(), result.getBulkMetadata().isPartialResponsePredicate());
    }

    private CommandMetadataResponse roundTrip(CommandMetadataResponse response) {
        Parcel parcel = Parcel.obtain();
        try {
            response.writeToParcel(parcel, 0);
            parcel.setDataPosition(0);

            CommandMetadataResponse result = CommandMetadataResponse.CREATOR.createFromParcel(parcel);

            assertEquals(0, parcel.dataAvail());

            return result;
        } finally {
            parcel.recycle();
        }
    }
}
