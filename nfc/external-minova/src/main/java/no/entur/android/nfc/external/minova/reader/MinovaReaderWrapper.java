package no.entur.android.nfc.external.minova.reader;

import no.entur.android.nfc.external.minova.service.MinovaService;

public class MinovaReaderWrapper {

    public void transmit(byte[] data) {
        MinovaService minovaService = new MinovaService(23);
        minovaService.transmit(data);
    }

}
