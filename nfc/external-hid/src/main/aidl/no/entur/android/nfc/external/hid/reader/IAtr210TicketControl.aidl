package no.entur.android.nfc.external.hid.reader;

interface IAtr210TicketControl {

    byte[] setResult(boolean valid, String led, String sound);

}
