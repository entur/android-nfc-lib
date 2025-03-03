package no.entur.android.nfc.detect;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.entur.android.nfc.detect.app.SelectApplicationAnalyzeResult;
import no.entur.android.nfc.detect.app.SelectApplicationAnalyzer;
import no.entur.android.nfc.detect.technology.TechnologyAnalyzeResult;
import no.entur.android.nfc.detect.technology.TechnologyAnalyzer;
import no.entur.android.nfc.detect.uid.UidAnalyzeResult;
import no.entur.android.nfc.detect.uid.UidAnalyzer;
import no.entur.android.nfc.detect.uid.UidManufacturerType;
import no.entur.android.nfc.detect.uid.UidSequenceType;
import no.entur.android.nfc.wrapper.Tag;

/**
 *
 * NFC tag analyzer. Attempts to determine the target nature by looking at tag metadata
 * and issuing commands to the card.
 * <br><br>
 * In a nutshell, the tech + UID analyzer is used to include or exclude results.
 *
 */

public class NfcTargetAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(NfcTargetAnalyzer.class);

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private List<Target> targets = new ArrayList<>();
        private TagTechnologiesFactory tagTechnologiesFactory;

        public Builder add(Consumer<TargetBuilder> builder) {
            TargetBuilder target = new TargetBuilder();
            builder.accept(target);
            targets.add(target.build());
            return this;
        }

        public Builder withTagTechnologiesFactory(TagTechnologiesFactory tagTechnologiesFactory) {
            this.tagTechnologiesFactory = tagTechnologiesFactory;
            return this;
        }

        public NfcTargetAnalyzer build() {
            if(tagTechnologiesFactory == null) {
                tagTechnologiesFactory = new DefaultTagTechnologiesFactory();
            }
            return new NfcTargetAnalyzer(targets, tagTechnologiesFactory);
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
        private boolean enabled;

        private TechnologyAnalyzer technologyAnalyzer;
        private UidAnalyzer uidAnalyzer;
        private SelectApplicationAnalyzer selectApplicationAnalyzer;

        public boolean isEnabled() {
            return enabled;
        }
    }

    protected static class TargetCandidate implements Comparable<TargetCandidate> {

        private Target target;
        private NfcTargetAnalyzeResult result;

        @Override
        public int compareTo(TargetCandidate o) {
            int technologyAnalyzeResult = result.getTechnologyAnalyzeResult().compareTo(o.result.getTechnologyAnalyzeResult());
            if(technologyAnalyzeResult != 0) {
                return technologyAnalyzeResult;
            }

            int uidAnalyzeResult = result.getUidAnalyzeResult().compareTo(o.result.getUidAnalyzeResult());
            if(uidAnalyzeResult != 0) {
                return uidAnalyzeResult;
            }

            return 0;
        }
    }

    protected final Map<String, Target> targets;

    // technologies to parse
    protected final Set<String> technologies;

    protected final TagTechnologiesFactory tagTechnologiesFactory;

    public NfcTargetAnalyzer(List<Target> targets, TagTechnologiesFactory tagTechnologiesFactory) {
        this.targets = new HashMap<>();
        for (Target target : targets) {
            this.targets.put(target.id, target);
        }
        this.technologies = toTechnologies(targets);

        this.tagTechnologiesFactory = tagTechnologiesFactory;
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

    public List<NfcTargetAnalyzeResult> analyze(Tag tag, Intent intent) throws IOException {
        TagTechnologies tagTechnologies = tagTechnologiesFactory.newInstance(tag, intent, technologies);

        if(tagTechnologies.isEmpty()) {
            // no point in continuing
            return Collections.emptyList();
        }

        // check technology and uid and use them to exclude candidates

        // then sort and check for application identifier

        List<TargetCandidate> candidates = new ArrayList<>();

        for (Map.Entry<String, Target> entry : targets.entrySet()) {
            Target target = entry.getValue();
            if(!target.isEnabled()) {
                continue;
            }
            TargetCandidate c = new TargetCandidate();

            c.target = target;
            c.result = new NfcTargetAnalyzeResult();
            c.result.setTagTechnologies(tagTechnologies);

            c.result.setId(target.id);

            candidates.add(c);
        }

        List<TargetCandidate> technologyResults = processTechnology(tag, intent, candidates, tagTechnologies);
        if(technologyResults.isEmpty()) {
            // no point in continuing
            LOGGER.info("Tag does not contain the relevant technologies");

            return Collections.emptyList();
        }

        List<TargetCandidate> uidResults = processUid(tag, intent, technologyResults, tagTechnologies);
        if(uidResults.isEmpty()) {
            // no point in continuing
            LOGGER.info("Tag does not contain the relevant UIDs");

            return Collections.emptyList();
        }

        // sort according to the most promising
        Collections.sort(uidResults);

        boolean applicationAnalyzer = isApplicationAnalyzer(uidResults);
        if(!applicationAnalyzer) {
            // ideally this would only be a single result
            return toResults(uidResults);
        }

        // returns first match
        List<TargetCandidate> selectApplicationResults = processSelectApplication(tag, intent, uidResults, tagTechnologies);
        if(selectApplicationResults.isEmpty()) {
            // no point in continuing
            LOGGER.info("Tag does not contain the relevant application(s)");

            return Collections.emptyList();
        }

        // can only be a single result
        return toResults(selectApplicationResults);
    }

    private List<TargetCandidate> processSelectApplication(Tag tag, Intent intent, List<TargetCandidate> candidates, TagTechnologies tagTechnologies) throws IOException {
        List<TargetCandidate> results = new ArrayList<>();
        for (TargetCandidate technologyTarget : candidates) {
            SelectApplicationAnalyzer selectApplicationAnalyzer = technologyTarget.target.selectApplicationAnalyzer;
            if(selectApplicationAnalyzer != null) {
                SelectApplicationAnalyzeResult result = selectApplicationAnalyzer.processApplication(tagTechnologies, tag, intent);

                if(result.isSuccess()) {
                    technologyTarget.result.setSelectApplicationAnalyzeResult(result);
                    results.add(technologyTarget);

                    break;
                }

            }

        }
        return results;
    }

    private static @NonNull List<NfcTargetAnalyzeResult> toResults(List<TargetCandidate> uidResults) {
        List<NfcTargetAnalyzeResult> output = new ArrayList<>();
        for (TargetCandidate uidResult : uidResults) {
            output.add(uidResult.result);
        }
        return output;
    }

    private boolean isApplicationAnalyzer(List<TargetCandidate> results) {
        for (TargetCandidate result : results) {
            if(result.target.selectApplicationAnalyzer != null) {
                return true;
            }
        }
        return false;
    }

    @NonNull
    private static List<TargetCandidate> processUid(Tag tag, Intent intent, List<TargetCandidate> candidates, TagTechnologies tagTechnologies) {
        List<TargetCandidate> results = new ArrayList<>();
        for (TargetCandidate technologyTarget : candidates) {
            UidAnalyzer uidAnalyzer = technologyTarget.target.uidAnalyzer;
            if(uidAnalyzer != null) {
                UidAnalyzeResult result = uidAnalyzer.processUid(tagTechnologies, tag, intent);

                boolean acceptableSequence = result.getSequenceType() == UidSequenceType.MATCH || result.getSequenceType() == UidSequenceType.NOT_AVAILABLE;
                boolean acceptableManufacturer = result.getManufacturerType() == UidManufacturerType.MATCH || result.getManufacturerType() == UidManufacturerType.NOT_AVAILABLE;

                if (result.isLength() && acceptableSequence && acceptableManufacturer) {
                    technologyTarget.result.setUidAnalyzeResult(result);
                    results.add(technologyTarget);
                }
            }

        }
        return results;
    }

    @NonNull
    private static List<TargetCandidate> processTechnology(Tag tag, Intent intent, List<TargetCandidate> candidates, TagTechnologies tagTechnologies) {
        List<TargetCandidate> results = new ArrayList<>();
        for (TargetCandidate technologyTarget : candidates) {
            TechnologyAnalyzer technologyAnalyzer = technologyTarget.target.technologyAnalyzer;
            TechnologyAnalyzeResult technologyAnalyzeResult = technologyAnalyzer.processTechnology(tagTechnologies, tag, intent);

            if(technologyAnalyzeResult.hasType()) {
                technologyTarget.result.setTechnologyAnalyzeResult(technologyAnalyzeResult);
                results.add(technologyTarget);
            }
        }
        return results;
    }

    public void enableExclusively(Set<String> ids) {
        for (Map.Entry<String, Target> entry : targets.entrySet()) {
            entry.getValue().enabled = ids.contains(entry.getKey());
        }
    }

    public void enableAll() {
        for (Map.Entry<String, Target> entry : targets.entrySet()) {
            entry.getValue().enabled = true;
        }
    }

    public void disableAll() {
        for (Map.Entry<String, Target> entry : targets.entrySet()) {
            entry.getValue().enabled = false;
        }
    }

    public void enable(String key) {
        Target target = targets.get(key);
        if(target == null) {
            throw new IllegalStateException();
        }
        target.enabled = true;
    }

    public void disable(String key) {
        Target target = targets.get(key);
        if(target == null) {
            throw new IllegalStateException();
        }
        target.enabled = false;
    }


}
