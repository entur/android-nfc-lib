package no.entur.android.nfc.wrapper.tech.utils.bulk.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.util.Arrays;

import no.entur.android.nfc.wrapper.tech.utils.bulk.FakeParcel;

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

        FakeParcel parcel = new FakeParcel();
        response.writeToParcel(parcel.getParcel(), 0);

        CommandMetadataResponse result = CommandMetadataResponse.CREATOR.createFromParcel(parcel.getParcel());

        assertEquals(response.getFormats(), result.getFormats());
        assertNull(result.getBulkMetadata());
        assertEquals(0, parcel.remaining());
    }

    private void assertRoundTrip(BulkTransceiveMetadata metadata) {
        CommandMetadataResponse response = new CommandMetadataResponse(Arrays.asList(CommandMetadataResponse.COMMAND_FORMAT_ISO7816, CommandMetadataResponse.COMMAND_FORMAT_NATIVE_MIFARE_DESFIRE), metadata);

        FakeParcel parcel = new FakeParcel();
        response.writeToParcel(parcel.getParcel(), 0);

        CommandMetadataResponse result = CommandMetadataResponse.CREATOR.createFromParcel(parcel.getParcel());

        assertEquals(response.getFormats(), result.getFormats());
        assertEquals(metadata.isResponsePredicate(), result.getBulkMetadata().isResponsePredicate());
        assertEquals(metadata.isPartialResponsePredicate(), result.getBulkMetadata().isPartialResponsePredicate());
        assertEquals(0, parcel.remaining());
    }
}
