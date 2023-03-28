package no.entur.android.nfc.external.minova.service;

import static no.entur.android.nfc.util.ByteArrayHexStringConverter.hexStringToByteArray;

import org.nfctools.api.TagType;

import no.entur.android.nfc.external.minova.reader.MinovaIsoDepWrapper;
import no.entur.android.nfc.external.tag.MifareDesfireTagServiceSupport;
import no.entur.android.nfc.tcpserver.CommandInputOutputThread;

public class MinovaService extends AbstractMinovaTcpService {

    protected MifareDesfireTagServiceSupport mifareDesfireTagServiceSupport;

    public MinovaService(int port, int readers) {
        super(port, readers);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        this.mifareDesfireTagServiceSupport = new MifareDesfireTagServiceSupport(this, binder, store);
    }

    @Override
    protected void handleTag(MinovaTagType tag, String uid, CommandInputOutputThread<String, String> io) {
        if (tag.getTagType() == TagType.ISO_DEP) {
            MinovaIsoDepWrapper wrapper = new MinovaIsoDepWrapper(io);
            mifareDesfireTagServiceSupport.desfire(-1, wrapper, hexStringToByteArray(uid), tag.getHistoricalBytes(), new MinovaIntentEnricher(io.getIp()));
        }
    }

}
