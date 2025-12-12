package no.entur.android.nfc.external.atr210.reader;

interface IAtr210BarcodeControl {

    byte[] setResult(boolean valid, String led, String sound);

}
