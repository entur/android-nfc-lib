package no.entur.android.nfc.external.acs.tag;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.nfctools.mf.block.DataBlock;
import org.nfctools.mf.block.MfBlock;
import org.nfctools.mf.ul.MfUlReaderWriter;
import org.nfctools.scio.Command;
import org.nfctools.scio.Response;

import java.io.IOException;

import no.entur.android.nfc.util.ByteArrayHexStringConverter;
import no.entur.android.nfc.wrapper.TransceiveResult;

public class AcsMifareUltralightCommandTechnologyTest {

    private static final byte[] GET_VERSION = new byte[] { 0x60 };

    private static final byte[] VERSION = ByteArrayHexStringConverter.hexStringToByteArray("0004040201000F03");

    private static class FakeReaderWriter implements MfUlReaderWriter {

        private byte[] passthroughRequest;
        private int readStartPage = -1;

        @Override
        public byte[] transmit(byte[] data) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Response transmit(Command command) {
            throw new UnsupportedOperationException();
        }

        @Override
        public MfBlock[] readBlock(int startPage, int pagesToRead) {
            this.readStartPage = startPage;

            MfBlock[] blocks = new MfBlock[pagesToRead];
            for (int i = 0; i < pagesToRead; i++) {
                byte page = (byte) (startPage + i);
                blocks[i] = new DataBlock(new byte[] { page, page, page, page });
            }
            return blocks;
        }

        @Override
        public void writeBlock(int startPage, MfBlock... mfBlock) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getMaxPagesPerRead() {
            return 4;
        }

        @Override
        public byte[] transmitPassthrough(byte[] data) {
            this.passthroughRequest = data;

            return VERSION;
        }
    }

    @Test
    public void testOtherCommandsUsePassthrough() throws Exception {
        FakeReaderWriter readerWriter = new FakeReaderWriter();
        AcsMifareUltralightCommandTechnology technology = new AcsMifareUltralightCommandTechnology(readerWriter, (e) -> new TransceiveResult(TransceiveResult.RESULT_FAILURE, null), true);

        TransceiveResult result = technology.transceive(GET_VERSION, true);

        assertArrayEquals(VERSION, result.getResponseOrThrow());
        assertArrayEquals(GET_VERSION, readerWriter.passthroughRequest);
    }

    @Test
    public void testOtherCommandsFailWithoutPassthrough() throws Exception {
        FakeReaderWriter readerWriter = new FakeReaderWriter();
        AcsMifareUltralightCommandTechnology technology = new AcsMifareUltralightCommandTechnology(readerWriter, (e) -> new TransceiveResult(TransceiveResult.RESULT_FAILURE, null));

        TransceiveResult result = technology.transceive(GET_VERSION, true);

        try {
            result.getResponseOrThrow();
            fail();
        } catch (IOException e) {
            // expected
        }
        assertNull(readerWriter.passthroughRequest);
    }

    @Test
    public void testReadDoesNotUsePassthrough() throws Exception {
        FakeReaderWriter readerWriter = new FakeReaderWriter();
        AcsMifareUltralightCommandTechnology technology = new AcsMifareUltralightCommandTechnology(readerWriter, (e) -> new TransceiveResult(TransceiveResult.RESULT_FAILURE, null), true);

        TransceiveResult result = technology.transceive(new byte[] { 0x30, 0x04 }, true);

        byte[] response = result.getResponseOrThrow();
        assertEquals(16, response.length);
        assertEquals(4, response[0]);
        assertEquals(7, response[15]);
        assertEquals(4, readerWriter.readStartPage);
        assertNull(readerWriter.passthroughRequest);
    }
}
