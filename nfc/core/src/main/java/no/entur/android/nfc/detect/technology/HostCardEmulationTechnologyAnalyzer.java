package no.entur.android.nfc.detect.technology;

import android.content.Intent;

import no.entur.android.nfc.detect.NfcTargetAnalyzer;
import no.entur.android.nfc.detect.TagTechnologies;
import no.entur.android.nfc.detect.TagTechnologiesFactory;
import no.entur.android.nfc.wrapper.Tag;
import no.entur.android.nfc.wrapper.tech.IsoDep;

public class HostCardEmulationTechnologyAnalyzer implements TechnologyAnalyzer {

    @Override
    public TechnologyAnalyzeResult processTechnology(TagTechnologies tagTechnologies, Tag tag, Intent intent) {

        if(tagTechnologies.hasIsoDep()) {
            IsoDep isoDep = tagTechnologies.getIsoDep();
            byte[] historicalBytes = isoDep.getHistoricalBytes();
            if(historicalBytes == null || historicalBytes.length == 0) {
                // TODO needs documentation
                return new TechnologyAnalyzeResult("hce");
            }
        }

        return new TechnologyAnalyzeResult(null);
    }

    @Override
    public String[] getTechnologies() {
        return new String[]{TagTechnologiesFactory.ISO_DEP};
    }

}
