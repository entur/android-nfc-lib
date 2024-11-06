package no.entur.android.nfc.detect.uid;

import android.content.Intent;

import no.entur.android.nfc.detect.TagTechnologies;
import no.entur.android.nfc.wrapper.Tag;

public class HostCardEmulationUidAnalyzer implements UidAnalyzer {

    @Override
    public UidAnalyzeResult processUid(TagTechnologies tagTechnologies, Tag tag, Intent intent) {

        byte[] id = tag.getId();
        if(id != null) {
            UidManufacturerType uidManufacturerType;

            if( (id[0] & 0xFF) == 0x08) {
                uidManufacturerType = UidManufacturerType.MATCH;
            } else {
                uidManufacturerType = UidManufacturerType.MISMATCH;
            }

            boolean length = id.length == 4;

            return new UidAnalyzeResult(length, UidSequenceType.NOT_AVAILABLE, uidManufacturerType);
        }

        return new UidAnalyzeResult(false, UidSequenceType.NOT_AVAILABLE, UidManufacturerType.NOT_AVAILABLE);
    }
}
