package no.entur.android.nfc.external.minova.service;

import static no.entur.android.nfc.util.ByteArrayHexStringConverter.hexStringToByteArray;

import org.nfctools.api.TagType;

import no.entur.android.nfc.external.minova.reader.MinovaCommandInputOutputThread;
import no.entur.android.nfc.external.minova.reader.MinovaIsoDepWrapper;
import no.entur.android.nfc.external.service.tag.TagProxy;
import no.entur.android.nfc.external.service.tag.TagTechnology;
import no.entur.android.nfc.external.tag.IsoDepTagServiceSupport;
import no.entur.android.nfc.tcpserver.CommandInputOutputThread;

public class MinovaService extends AbstractMinovaTcpService {

    protected IsoDepTagServiceSupport isoDepTagServiceSupport;

    public MinovaService(int port, int readers) {
        super(port, readers);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        this.isoDepTagServiceSupport = new IsoDepTagServiceSupport(this, binder, store);
    }

    @Override
    protected void handleTag(MinovaTagType tag, String uid, CommandInputOutputThread<String, String> reader) {

        // enforce that only one tag is present at the same time
        // there is no "tag lost", so invalidate the previous tag whenever a new tag is detected
        MinovaCommandInputOutputThread minovaCommandInputOutputThread = (MinovaCommandInputOutputThread) reader;
        TagProxy currentTagProxy = minovaCommandInputOutputThread.getCurrentTagProxy();
        if(currentTagProxy != null) {
            store.remove(currentTagProxy);

            minovaCommandInputOutputThread.setCurrentTagProxy(null);
        }

        if (tag.getTagType() == TagType.DESFIRE_EV1 || tag.getTagType() == TagType.ISO_DEP) {
            MinovaIsoDepWrapper wrapper = new MinovaIsoDepWrapper(reader);
            // might be null
            TagProxy nextTagProxy = isoDepTagServiceSupport.card(-1, wrapper, hexStringToByteArray(uid), tag.getHistoricalBytes(), new MinovaIntentEnricher(reader.getIp()));
            minovaCommandInputOutputThread.setCurrentTagProxy(nextTagProxy);
        }
    }

}
