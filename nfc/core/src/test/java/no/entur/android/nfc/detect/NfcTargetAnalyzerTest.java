package no.entur.android.nfc.detect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import no.entur.android.nfc.detect.app.SelectApplicationAnalyzeResult;
import no.entur.android.nfc.detect.app.SelectApplicationAnalyzer;
import no.entur.android.nfc.detect.technology.TechnologyAnalyzeResult;
import no.entur.android.nfc.detect.technology.TechnologyAnalyzer;
import no.entur.android.nfc.detect.uid.SevenByteNxpUidAnalyzer;
import no.entur.android.nfc.wrapper.Tag;
import no.entur.android.nfc.wrapper.tech.IsoDep;

public class NfcTargetAnalyzerTest {

    private static final byte[] NXP_UID = new byte[] { 0x04, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06 };

    private static TechnologyAnalyzer technology(String type) {
        return new TechnologyAnalyzer() {
            @Override
            public TechnologyAnalyzeResult processTechnology(TagTechnologies tagTechnologies, Tag tag, android.content.Intent intent) {
                // null means the tag is guaranteed not to match
                return type != null ? new TechnologyAnalyzeResult(type) : null;
            }

            @Override
            public String[] getTechnologies() {
                return new String[] { TagTechnologiesFactory.ISO_DEP };
            }
        };
    }

    private static SelectApplicationAnalyzer application(Boolean success) {
        return (tagTechnologies, tag, intent) -> success != null ? new SelectApplicationAnalyzeResult(success, new byte[0], new byte[0], new byte[0]) : null;
    }

    private static NfcTargetAnalyzer.Builder builder() {
        TagTechnologies tagTechnologies = new TagTechnologies();
        tagTechnologies.setIsoDep(mock(IsoDep.class));
        return NfcTargetAnalyzer.newBuilder().withTagTechnologiesFactory((tag, intent, technologies) -> tagTechnologies);
    }

    private static List<String> ids(List<NfcTargetAnalyzeResult> results) {
        List<String> ids = new ArrayList<>();
        for (NfcTargetAnalyzeResult result : results) {
            ids.add(result.getId());
        }
        return ids;
    }

    private static Tag tag() {
        Tag tag = mock(Tag.class);
        when(tag.getId()).thenReturn(NXP_UID);
        return tag;
    }

    @Test
    public void testTargetWithoutUidAnalyzer() throws Exception {
        NfcTargetAnalyzer analyzer = builder()
                .add((c) -> c.withId("a").withTechnologyAnalyzer(technology("isodep")))
                .build();
        analyzer.enableAll();

        assertEquals(java.util.Collections.singletonList("a"), ids(analyzer.analyze(tag(), null)));
    }

    @Test
    public void testTargetsWithAndWithoutUidAnalyzer() throws Exception {
        NfcTargetAnalyzer analyzer = builder()
                .add((c) -> c.withId("any").withTechnologyAnalyzer(technology("isodep")))
                .add((c) -> c.withId("nxp").withTechnologyAnalyzer(technology("isodep")).withUidAnalyzer(new SevenByteNxpUidAnalyzer()))
                .build();
        analyzer.enableAll();

        // the target with a matching UID is more specific
        assertEquals(java.util.Arrays.asList("nxp", "any"), ids(analyzer.analyze(tag(), null)));
    }

    @Test
    public void testTargetsWithoutId() throws Exception {
        NfcTargetAnalyzer analyzer = builder()
                .add((c) -> c.withTechnologyAnalyzer(technology("isodep")))
                .add((c) -> c.withTechnologyAnalyzer(technology("isodep")))
                .build();
        analyzer.enableAll();

        List<NfcTargetAnalyzeResult> results = analyzer.analyze(tag(), null);
        assertEquals(2, results.size());
        assertNull(results.get(0).getId());
    }

    @Test
    public void testDuplicateId() {
        NfcTargetAnalyzer.Builder builder = builder()
                .add((c) -> c.withId("a").withTechnologyAnalyzer(technology("isodep")))
                .add((c) -> c.withId("a").withTechnologyAnalyzer(technology("isodep")));
        try {
            builder.build();
            fail();
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void testTechnologyAnalyzerReturnsNull() throws Exception {
        NfcTargetAnalyzer analyzer = builder()
                .add((c) -> c.withId("none").withTechnologyAnalyzer(technology(null)))
                .add((c) -> c.withId("a").withTechnologyAnalyzer(technology("isodep")))
                .build();
        analyzer.enableAll();

        assertEquals(java.util.Collections.singletonList("a"), ids(analyzer.analyze(tag(), null)));
    }

    @Test
    public void testApplicationMatch() throws Exception {
        NfcTargetAnalyzer analyzer = builder()
                .add((c) -> c.withId("plain").withTechnologyAnalyzer(technology("isodep")))
                .add((c) -> c.withId("app").withTechnologyAnalyzer(technology("isodep")).withSelectApplicationAnalyzer(application(true)))
                .build();
        analyzer.enableAll();

        assertEquals(java.util.Collections.singletonList("app"), ids(analyzer.analyze(tag(), null)));
    }

    @Test
    public void testApplicationMismatchFallsBackToTargetWithoutApplication() throws Exception {
        NfcTargetAnalyzer analyzer = builder()
                .add((c) -> c.withId("app").withTechnologyAnalyzer(technology("isodep")).withSelectApplicationAnalyzer(application(false)))
                .add((c) -> c.withId("nullResult").withTechnologyAnalyzer(technology("isodep")).withSelectApplicationAnalyzer(application(null)))
                .add((c) -> c.withId("plain").withTechnologyAnalyzer(technology("isodep")))
                .build();
        analyzer.enableAll();

        assertEquals(java.util.Collections.singletonList("plain"), ids(analyzer.analyze(tag(), null)));
    }

    @Test
    public void testApplicationMismatch() throws Exception {
        NfcTargetAnalyzer analyzer = builder()
                .add((c) -> c.withId("app").withTechnologyAnalyzer(technology("isodep")).withSelectApplicationAnalyzer(application(false)))
                .build();
        analyzer.enableAll();

        assertTrue(analyzer.analyze(tag(), null).isEmpty());
    }
}
