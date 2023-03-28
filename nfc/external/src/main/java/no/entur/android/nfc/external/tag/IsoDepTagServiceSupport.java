package no.entur.android.nfc.external.tag;

import android.content.Context;
import android.content.Intent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import no.entur.android.nfc.external.ExternalNfcTagCallback;
import no.entur.android.nfc.external.service.tag.TagProxyStore;
import no.entur.android.nfc.external.service.tag.TagTechnology;
import no.entur.android.nfc.wrapper.INfcTag;

public class IsoDepTagServiceSupport extends AbstractTagServiceSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(IsoDepTagServiceSupport.class);

    protected IsoDepTagFactory isoDepTagFactory = new IsoDepTagFactory();

    public IsoDepTagServiceSupport(Context context, INfcTag tagService, TagProxyStore store) {
        super(context, tagService, store);
    }

    public void hce(int slotNumber, AbstractReaderIsoDepWrapper wrapper, byte[] uid, byte[] historicalBytes, IntentEnricher extras) {
        try {
            List<TagTechnology> technologies = new ArrayList<>();
            technologies.add(new NfcAAdapter(wrapper, true));
            technologies.add(new IsoDepAdapter(wrapper, true));

            int serviceHandle = store.add(slotNumber, technologies);

            Intent intent = isoDepTagFactory.getTag(serviceHandle, null, uid, true, historicalBytes, tagService, extras);

            LOGGER.debug("Broadcast hce");

            context.sendBroadcast(intent, ANDROID_PERMISSION_NFC);
        } catch (Exception e) {
            LOGGER.debug("Problem reading from tag", e);

            broadcast(ExternalNfcTagCallback.ACTION_TECH_DISCOVERED);
        }
    }

    public void card(int slotNumber, AbstractReaderIsoDepWrapper wrapper, byte[] uid, byte[] historicalBytes, IntentEnricher extras) {
        try {
            List<TagTechnology> technologies = new ArrayList<>();
            technologies.add(new NfcAAdapter(wrapper, false));
            technologies.add(new IsoDepAdapter(wrapper, false));

            int serviceHandle = store.add(slotNumber, technologies);

            Intent intent = isoDepTagFactory.getTag(serviceHandle, null, uid, false, historicalBytes, tagService, extras);

            LOGGER.debug("Broadcast IsoDep tag");

            context.sendBroadcast(intent, ANDROID_PERMISSION_NFC);
        } catch (Exception e) {
            LOGGER.debug("Problem reading from tag", e);

            broadcast(ExternalNfcTagCallback.ACTION_TECH_DISCOVERED);
        }
    }

}
