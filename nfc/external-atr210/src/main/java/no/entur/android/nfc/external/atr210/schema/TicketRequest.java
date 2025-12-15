package no.entur.android.nfc.external.atr210.schema;

public class TicketRequest extends AbstractMessage {

    private String barcode;

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }
}
