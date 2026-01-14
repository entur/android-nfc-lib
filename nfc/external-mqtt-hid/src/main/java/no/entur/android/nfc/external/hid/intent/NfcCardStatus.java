package no.entur.android.nfc.external.hid.intent;

public enum NfcCardStatus {

    UNAWARE, IGNORE, CHANGED,
    UNKNOWN, UNAVAILABLE,
    EMPTY, PRESENT, EXCLUSIVE,
    INUSE, MUTE, UNPOWERED;

    private static NfcCardStatus[] VALUES = NfcCardStatus.values();

    public static NfcCardStatus fromOrdinal(int i) {
        return VALUES[i];
    }
}
