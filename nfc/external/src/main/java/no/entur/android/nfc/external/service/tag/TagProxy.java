package no.entur.android.nfc.external.service.tag;

import java.util.List;

import no.entur.android.nfc.wrapper.TagImpl;

public interface TagProxy {
    int getHandle();

    void closeTechnology();

    void setPresent(boolean b);

    int getSlotNumber();

    TagTechnology getCurrent();

    void setCurrent(TagTechnology o);

    boolean selectTechnology(int technology);

    List<TagTechnology> getTechnologies();

    boolean isNdef();

    boolean isPresent();

    boolean ndefIsWritable();

    TagImpl rediscover(Object inFcTagBinder);

    void close();

    void setUid(byte[] uid);

    byte[] getUid();
}
