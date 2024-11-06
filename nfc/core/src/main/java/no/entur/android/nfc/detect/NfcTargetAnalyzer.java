package no.entur.android.nfc.detect;

import android.content.Intent;

import androidx.core.util.Consumer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import no.entur.android.nfc.detect.app.SelectApplicationAnalyzer;
import no.entur.android.nfc.detect.technology.TechnologyAnalyzer;
import no.entur.android.nfc.detect.uid.UidAnalyzer;
import no.entur.android.nfc.wrapper.Tag;
import no.entur.android.nfc.wrapper.tech.IsoDep;
import no.entur.android.nfc.wrapper.tech.MifareClassic;
import no.entur.android.nfc.wrapper.tech.MifareUltralight;
import no.entur.android.nfc.wrapper.tech.NfcA;
import no.entur.android.nfc.wrapper.tech.NfcB;
import no.entur.android.nfc.wrapper.tech.NfcF;
import no.entur.android.nfc.wrapper.tech.NfcV;

/**
 *
 * IsoDep tag analyzer. Attempts to determine the target nature by looking at tag metadata
 * and issuing commands to the card.
 * <br><br>
 * In a nutshell, the tech + UID analyzer is used to include or exclude results.
 *
 */

public class NfcTargetAnalyzer {

    private static final String ISO_DEP = "android.nfc.tech.IsoDep";
    private static final String MIFARE_CLASSIC = "android.nfc.tech.MifareClassic";
    private static final String MIFARE_ULTRALIGHT = "android.nfc.tech.MifareUltralight";
    private static final String NFC_A = "android.nfc.tech.NfcA";
    private static final String NFC_B = "android.nfc.tech.NfcB";
    private static final String NFC_F = "android.nfc.tech.NfcF";
    private static final String NFC_V = "android.nfc.tech.NfcV";

    private static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private List<Target> targets = new ArrayList<>();

        public Builder add(Consumer<TargetBuilder> builder) {
            TargetBuilder target = new TargetBuilder();
            builder.accept(target);
            targets.add(target.build());
            return this;
        }

        public NfcTargetAnalyzer build() {
            return new NfcTargetAnalyzer(targets);
        }

    }

    public static class TargetBuilder {

        private String id;

        private TechnologyAnalyzer technologyAnalyzer;

        private SelectApplicationAnalyzer selectApplicationAnalyzer;

        private UidAnalyzer uidAnalyzer;

        public TargetBuilder withTechnologyAnalyzer(TechnologyAnalyzer technologyAnalyzer) {
            this.technologyAnalyzer = technologyAnalyzer;
            return this;
        }

        public TargetBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public TargetBuilder withSelectApplicationAnalyzer(SelectApplicationAnalyzer selectApplicationAnalyzer) {
            this.selectApplicationAnalyzer = selectApplicationAnalyzer;
            return this;
        }

        public TargetBuilder withUidAnalyzer(UidAnalyzer uidAnalyzer) {
            this.uidAnalyzer = uidAnalyzer;
            return this;
        }

        Target build() {
            Target target = new Target();

            if(technologyAnalyzer == null) {
                throw new IllegalStateException("Expected technology analyzer");
            }

            target.id = id;
            target.technologyAnalyzer = technologyAnalyzer;
            target.selectApplicationAnalyzer = selectApplicationAnalyzer;
            target.uidAnalyzer = uidAnalyzer;
            return target;
        }

    }

    protected static class Target {

        private String id;

        private TechnologyAnalyzer technologyAnalyzer;

        private UidAnalyzer uidAnalyzer;

        private SelectApplicationAnalyzer selectApplicationAnalyzer;

    }

    private final List<Target> targets;

    // technologies to parse
    private final Set<String> technologies;

    public NfcTargetAnalyzer(List<Target> targets) {
        this.targets = targets;

        technologies = toTechnologies(targets);
    }

    private Set<String> toTechnologies(List<Target> targets) {
        Set<String> set = new HashSet<>();

        for (Target target : targets) {
            String[] technologies = target.technologyAnalyzer.getTechnologies();
            for (String technology : technologies) {
                set.add(technology);
            }
        }
        return set;
    }

    public NfcTargetAnalyzeResult analyze(Tag tag, Intent intent) {
        TagTechnologies tagTechnologies = getTagTechnologies(tag);

        if(tagTechnologies.isEmpty()) {
            // no point in continuing
            return null;
        }

        // check technology and uid and use them to exclude candidates

        // then sort and check for application identifier





        return null;
    }

    private TagTechnologies getTagTechnologies(Tag tag) {
        TagTechnologies tech = new TagTechnologies();

        String[] techList = tag.getTechList();

        for (String s : techList) {
            if(technologies.contains(s)) {
                switch(s) {
                    case ISO_DEP: {
                        tech.setIsoDep(IsoDep.get(tag));
                        break;
                    }
                    case MIFARE_CLASSIC: {
                        tech.setMifareClassic(MifareClassic.get(tag));
                        break;
                    }
                    case MIFARE_ULTRALIGHT: {
                        tech.setMifareUltralight(MifareUltralight.get(tag));
                        break;
                    }
                    case NFC_A: {
                        tech.setNfcA(NfcA.get(tag));
                        break;
                    }
                    case NFC_B: {
                        tech.setNfcB(NfcB.get(tag));
                        break;
                    }
                    case NFC_F: {
                        tech.setNfcF(NfcF.get(tag));
                        break;
                    }
                    case NFC_V: {
                        tech.setNfcV(NfcV.get(tag));
                        break;
                    }
                }
            }
        }
        return tech;
    }


}
