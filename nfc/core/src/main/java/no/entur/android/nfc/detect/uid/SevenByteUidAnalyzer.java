package no.entur.android.nfc.detect.uid;

import android.content.Intent;

import no.entur.android.nfc.detect.TagTechnologies;
import no.entur.android.nfc.wrapper.Tag;

public class SevenByteUidAnalyzer implements UidAnalyzer {

    @Override
    public UidAnalyzeResult processUid(TagTechnologies tagTechnologies, Tag tag, Intent intent) {

        byte[] id = tag.getId();
        if(id != null) {
            boolean length = id.length == 7;

            return new UidAnalyzeResult(length, UidSequenceType.NOT_AVAILABLE, UidManufacturerType.NOT_AVAILABLE);
        }

        return new UidAnalyzeResult(false, UidSequenceType.NOT_AVAILABLE, UidManufacturerType.NOT_AVAILABLE);
    }
}
