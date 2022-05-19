package no.entur.android.nfc.external.minova.service;

import static no.entur.android.nfc.util.ByteArrayHexStringConverter.hexStringToByteArray;

import org.nfctools.api.TagType;

import no.entur.android.nfc.external.minova.reader.MinovaIsoDepWrapper;
import no.entur.android.nfc.external.tag.MifareDesfireTagServiceSupport;
import no.entur.android.nfc.tcpserver.CommandInputOutputThread;

public class MinovaService extends AbstractMinovaTcpService {

    protected MifareDesfireTagServiceSupport mifareDesfireTagServiceSupport;

    @Override
    public void onCreate() {
        super.onCreate();

        this.mifareDesfireTagServiceSupport = new MifareDesfireTagServiceSupport(this, binder, store);
    }

    @Override
    protected void handleTag(int slot, TagType tag, byte[] atr, String uid, CommandInputOutputThread<String, String> io) {
        if (tag == TagType.DESFIRE_EV1) {
            MinovaIsoDepWrapper wrapper = new MinovaIsoDepWrapper(io, slot);
            mifareDesfireTagServiceSupport.desfire(slot, atr, wrapper, hexStringToByteArray(uid));
        }
    }

}
