package no.entur.android.nfc.external.service.tag;

import java.util.List;

public interface TagProxyStore {
    TagProxy add(int slotNumber, List<TagTechnology> technologies);

    boolean remove(TagProxy defaultTagProxy);

    TagProxy get(int serviceHandle);
}
