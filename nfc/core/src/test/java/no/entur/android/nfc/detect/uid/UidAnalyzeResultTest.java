package no.entur.android.nfc.detect.uid;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UidAnalyzeResultTest {

    @Test
    public void testSort() {
        UidAnalyzeResult best = new UidAnalyzeResult(true, UidSequenceType.MATCH, UidManufacturerType.MATCH);

        UidAnalyzeResult b = new UidAnalyzeResult(true, UidSequenceType.MATCH, UidManufacturerType.MATCH);
        UidAnalyzeResult c = new UidAnalyzeResult(true, UidSequenceType.NOT_AVAILABLE, UidManufacturerType.MATCH);

        UidAnalyzeResult d = new UidAnalyzeResult(false, UidSequenceType.MATCH, UidManufacturerType.MATCH);
        UidAnalyzeResult e = new UidAnalyzeResult(false, UidSequenceType.NOT_AVAILABLE, UidManufacturerType.MATCH);

        UidAnalyzeResult worst = new UidAnalyzeResult(false, UidSequenceType.MISMATCH, UidManufacturerType.MISMATCH);

        List<UidAnalyzeResult> expected = Arrays.asList(best, b, c, d, e, worst);

        List<UidAnalyzeResult> list = Arrays.asList(best, b, c, d, e, worst);

        for(int i = 0; i < 100; i++) {
            Collections.shuffle(list);
            Collections.sort(list);

            assertEquals(expected, list);
        }

    }
}
