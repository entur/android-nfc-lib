package no.entur.android.nfc.external.minova.service;

import static no.entur.android.nfc.util.ByteArrayHexStringConverter.hexStringToByteArray;

import android.content.Intent;

import org.nfctools.api.TagType;

import no.entur.android.nfc.external.ExternalNfcTagCallback;
import no.entur.android.nfc.external.minova.reader.MinovaCommandInputOutputThread;
import no.entur.android.nfc.external.minova.reader.MinovaIsoDepWrapper;
import no.entur.android.nfc.external.service.tag.TagProxy;
import no.entur.android.nfc.external.service.tag.TagTechnology;
import no.entur.android.nfc.external.tag.IsoDepTagServiceSupport;
import no.entur.android.nfc.tcpserver.CommandInputOutputThread;

public class MinovaService extends AbstractMinovaTcpService {

    public static final String EXTRA_TAG_LEFT_FIELD_REASON = AbstractMinovaTcpService.class.getName() + ".extra.ACTION_TAG_LEFT_FIELD_REASON";
    public static final String EXTRA_TAG_LEFT_FIELD_REASON_NEW_TAG = "NEW_TAG";
    public static final String EXTRA_TAG_LEFT_FIELD_REASON_TRANSCEIVE_FAILED = "TRANSCEIVE_FAILED";

    public static final String EXTRA_TAG_LEFT_FIELD_SERVICE_HANDLE = AbstractMinovaTcpService.class.getName() + ".extra.EXTRA_TAG_LEFT_FIELD_SERVICE_HANDLE";

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
            currentTagProxy.close();

            Intent intent = new Intent();
            intent.setAction(ExternalNfcTagCallback.ACTION_TAG_LEFT_FIELD);
            intent.putExtra(EXTRA_TAG_LEFT_FIELD_REASON, EXTRA_TAG_LEFT_FIELD_REASON_NEW_TAG);
            intent.putExtra(EXTRA_TAG_LEFT_FIELD_SERVICE_HANDLE, currentTagProxy.getHandle());

            sendBroadcast(intent, "android.permission.NFC");

            minovaCommandInputOutputThread.setCurrentTagProxy(null);
        }

        if (tag.getTagType() == TagType.DESFIRE_EV1 || tag.getTagType() == TagType.ISO_DEP) {
            MinovaIsoDepWrapper wrapper = new MinovaIsoDepWrapper(reader, this);
            // might be null
            TagProxy nextTagProxy = isoDepTagServiceSupport.card(-1, wrapper, hexStringToByteArray(uid), tag.getHistoricalBytes(), new MinovaIntentEnricher(reader.getIp()));
            minovaCommandInputOutputThread.setCurrentTagProxy(nextTagProxy);
        }
    }

}
