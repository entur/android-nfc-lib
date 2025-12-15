package no.entur.android.nfc.external.atr210.schema;

public enum Status {

    UNAWARE, IGNORE, CHANGED,
    UNKNOWN, UNAVAILABLE,
    EMPTY, PRESENT, EXCLUSIVE,
    INUSE, MUTE, UNPOWERED;

    private static Status[] VALUES = Status.values();

    public static Status fromOrdinal(int i) {
        return VALUES[i];
    }
}
