package no.entur.android.nfc.hce;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;

import no.entur.android.nfc.CommandAPDU;

public class ResponseApduProtocolHostApduServiceTest {

    private static class TestService extends AbstractResponseApduProtocolHostApduService {

        @Override
        protected byte[] handleCommandApdu(CommandAPDU command) {
            return RESPONSE_OK;
        }

        @Override
        protected byte[] handleApplicationSelectedCommandApdu() {
            return RESPONSE_OK;
        }

        void setProtocol(ResponseApduProtocol protocol) {
            this.protocol = protocol;
        }
    }

    private static final byte[] SELECT = new byte[] { 0x00, (byte) 0xA4, 0x04, 0x00, 0x02, 0x01, 0x02 };

    @Test
    public void testMalformedApdu() {
        TestService service = new TestService();

        // too short
        assertArrayEquals(new byte[] { 0x67, 0x00 }, service.processCommandApdu(new byte[] { 0x00 }, null));
        // Lc does not match the length
        assertArrayEquals(new byte[] { 0x67, 0x00 }, service.processCommandApdu(new byte[] { 0x00, 0x01, 0x00, 0x00, 0x05, 0x01 }, null));
    }

    @Test
    public void testProtocolWithoutResponse() {
        TestService service = new TestService();

        ResponseApduProtocol protocol = mock(ResponseApduProtocol.class);
        when(protocol.handleCommandApdu(any())).thenReturn(null);
        service.setProtocol(protocol);

        assertArrayEquals(new byte[] { 0x68, 0x00 }, service.processCommandApdu(new byte[] { 0x00, 0x01, 0x00, 0x00 }, null));
    }

    @Test
    public void testSelectClosesPreviousProtocol() {
        TestService service = new TestService();

        ResponseApduProtocol protocol = mock(ResponseApduProtocol.class);
        service.setProtocol(protocol);

        assertArrayEquals(new byte[] { (byte) 0x90, 0x00 }, service.processCommandApdu(SELECT, null));

        verify(protocol).close();
    }
}
