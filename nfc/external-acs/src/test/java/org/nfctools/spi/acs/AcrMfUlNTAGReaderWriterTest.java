package org.nfctools.spi.acs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.nfctools.mf.block.MfBlock;
import org.nfctools.mf.ul.ntag.NfcNtag;

import java.util.ArrayList;
import java.util.List;

public class AcrMfUlNTAGReaderWriterTest {

    /**
     * Simulates FAST_READ on a tag where each page is filled with its own page number.
     */

    private static class FastReadNtag extends NfcNtag {

        private final List<int[]> ranges = new ArrayList<>();

        public FastReadNtag() {
            super(null);
        }

        @Override
        public byte[] fastRead(int startAddr, int endAddr) {
            ranges.add(new int[] { startAddr, endAddr });

            // start and end page are inclusive
            int pages = endAddr - startAddr + 1;
            byte[] data = new byte[pages * 4];
            for (int i = 0; i < pages; i++) {
                for (int k = 0; k < 4; k++) {
                    data[i * 4 + k] = (byte) (startAddr + i);
                }
            }
            return data;
        }
    }

    @Test
    public void testReadBlockInMultipleChunks() throws Exception {
        FastReadNtag ntag = new FastReadNtag();
        AcrMfUlNTAGReaderWriter readerWriter = new AcrMfUlNTAGReaderWriter(null, ntag, 0);

        int pagesPerRead = readerWriter.getMaxPagesPerRead();
        int startPage = 4;
        int pagesToRead = pagesPerRead + 10;

        MfBlock[] blocks = readerWriter.readBlock(startPage, pagesToRead);

        assertEquals(pagesToRead, blocks.length);
        for (int i = 0; i < blocks.length; i++) {
            assertNotNull("Block " + i, blocks[i]);
            assertEquals("Block " + i, (byte) (startPage + i), blocks[i].getData()[0]);
        }

        assertEquals(2, ntag.ranges.size());
        assertEquals(startPage, ntag.ranges.get(0)[0]);
        assertEquals(startPage + pagesPerRead - 1, ntag.ranges.get(0)[1]);
        assertEquals(startPage + pagesPerRead, ntag.ranges.get(1)[0]);
        assertEquals(startPage + pagesToRead - 1, ntag.ranges.get(1)[1]);
    }
}
