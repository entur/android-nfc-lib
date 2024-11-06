package no.entur.android.nfc.detect.technology;

import android.content.Intent;

import no.entur.android.nfc.detect.NfcTargetAnalyzer;
import no.entur.android.nfc.detect.TagTechnologies;
import no.entur.android.nfc.detect.TagTechnologiesFactory;
import no.entur.android.nfc.wrapper.Tag;

public class EnvTechnologyAnalyzer implements TechnologyAnalyzer {

    @Override
    public TechnologyAnalyzeResult processTechnology(TagTechnologies tagTechnologies, Tag tag, Intent intent) {

        if(tagTechnologies.hasIsoDep()) {
            // TODO documentation
            return new TechnologyAnalyzeResult("env");
        }

        return new TechnologyAnalyzeResult(null);
    }

    @Override
    public String[] getTechnologies() {
        return new String[]{TagTechnologiesFactory.ISO_DEP};
    }

}
