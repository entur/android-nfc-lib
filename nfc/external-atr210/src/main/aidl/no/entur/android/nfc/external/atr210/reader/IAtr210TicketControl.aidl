package no.entur.android.nfc.external.atr210.reader;

interface IAtr210TicketControl {

    byte[] setResult(boolean valid, String led, String sound);

}
