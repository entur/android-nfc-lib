package no.entur.android.nfc.detect.technology;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TechnologyAnalyzeResultTest {

    @Test
    public void testSort() {
        TechnologyAnalyzeResult best = new TechnologyAnalyzeResult("iso");
        TechnologyAnalyzeResult worst = new TechnologyAnalyzeResult(null);

        List<TechnologyAnalyzeResult> expected = Arrays.asList(best, worst);

        List<TechnologyAnalyzeResult> list1 = Arrays.asList(best, worst);
        List<TechnologyAnalyzeResult> list2 = Arrays.asList(worst, best);

        Collections.sort(list1);
        Collections.sort(list2);

        assertEquals(expected, list1);
        assertEquals(expected, list2);
    }
}
