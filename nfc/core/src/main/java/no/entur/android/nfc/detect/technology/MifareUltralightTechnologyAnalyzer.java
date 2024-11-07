package no.entur.android.nfc.detect.technology;

import android.content.Intent;

import no.entur.android.nfc.detect.TagTechnologies;
import no.entur.android.nfc.detect.TagTechnologiesFactory;
import no.entur.android.nfc.wrapper.Tag;
import no.entur.android.nfc.wrapper.tech.IsoDep;
import no.entur.android.nfc.wrapper.tech.MifareUltralight;

public class MifareUltralightTechnologyAnalyzer implements TechnologyAnalyzer {

    private final int type;
    private final String id;

    public MifareUltralightTechnologyAnalyzer(int type, String id) {
        this.type = type;
        this.id = id;
    }

    @Override
    public TechnologyAnalyzeResult processTechnology(TagTechnologies tagTechnologies, Tag tag, Intent intent) {

        if(tagTechnologies.hasMifareUltralight() && tagTechnologies.hasNfcA()) {
            MifareUltralight mifareUltralight = tagTechnologies.getMifareUltralight();

            if(type == mifareUltralight.getType()) {
                return new TechnologyAnalyzeResult(id);
            }
        }

        return new TechnologyAnalyzeResult(null);
    }

    @Override
    public String[] getTechnologies() {
        return new String[]{TagTechnologiesFactory.MIFARE_ULTRALIGHT, TagTechnologiesFactory.NFC_A};
    }

}
