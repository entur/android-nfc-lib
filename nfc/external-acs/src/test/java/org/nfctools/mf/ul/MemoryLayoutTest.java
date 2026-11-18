package org.nfctools.mf.ul;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class MemoryLayoutTest {

    @Test
    public void testNtagSizes() {
        // user memory (bytes) and last page as per the NXP datasheets
        assertLayout(MemoryLayout.NTAG203, 144, 0x29);
        assertLayout(MemoryLayout.NTAG210, 48, 0x13);
        assertLayout(MemoryLayout.NTAG212, 128, 0x28);
        assertLayout(MemoryLayout.NTAG213, 144, 0x2C);
        assertLayout(MemoryLayout.NTAG215, 504, 0x86);
        assertLayout(MemoryLayout.NTAG216, 888, 0xE6);
        assertLayout(MemoryLayout.ULTRALIGHT, 48, 0x0F);
        assertLayout(MemoryLayout.ULTRALIGHT_C, 144, 0x2F);
    }

    @Test
    public void testDistinctTypes() {
        assertNotEquals(MemoryLayout.TYPE_ULTRALIGHT_C, MemoryLayout.TYPE_NTAG_203);
        assertNotEquals(MemoryLayout.ULTRALIGHT_C.getType(), MemoryLayout.NTAG203.getType());
    }

    private static void assertLayout(MemoryLayout layout, int userMemory, int lastPage) {
        assertEquals(4, layout.getFirstDataPage());
        assertEquals(userMemory, layout.getMaxSize());
        assertEquals(lastPage, layout.getLastPage());
    }
}
