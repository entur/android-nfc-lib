package no.entur.android.nfc.detect.uid;

public enum UidSequenceType implements Comparable<UidSequenceType>{

    NOT_AVAILABLE, // i.e. not available or relevant
    INSIDE,
    OUTSIDE;
}
