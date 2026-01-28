package no.entur.android.nfc.external.hid.dto.atr210;

public class TicketRequest extends AbstractMessage {

    private String barcode;

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }
}
