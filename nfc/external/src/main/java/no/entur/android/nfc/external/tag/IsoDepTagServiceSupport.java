package no.entur.android.nfc.external.tag;

import android.content.Context;
import android.content.Intent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import no.entur.android.nfc.external.ExternalNfcTagCallback;
import no.entur.android.nfc.external.service.tag.TagProxy;
import no.entur.android.nfc.external.service.tag.TagProxyStore;
import no.entur.android.nfc.external.service.tag.TagTechnology;
import no.entur.android.nfc.wrapper.INfcTag;

public class IsoDepTagServiceSupport extends AbstractTagServiceSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(IsoDepTagServiceSupport.class);

    protected IsoDepTagFactory isoDepTagFactory = new IsoDepTagFactory();
    protected TransceiveResultExceptionMapper transceiveResultExceptionMapper;

    public IsoDepTagServiceSupport(Context context, INfcTag tagService, TagProxyStore store, TransceiveResultExceptionMapper transceiveResultExceptionMapper) {
        super(context, tagService, store);

        this.transceiveResultExceptionMapper = transceiveResultExceptionMapper;
    }

    public TagProxy hce(int slotNumber, AbstractReaderIsoDepWrapper wrapper, byte[] uid, byte[] historicalBytes, IntentEnricher extras) {
        try {
            List<TagTechnology> technologies = new ArrayList<>();

            technologies.add(new WrapNfcACommandTechnology(wrapper,  transceiveResultExceptionMapper, true));
            technologies.add(new IsoDepCommandTechnology(wrapper, true,  transceiveResultExceptionMapper));

            TagProxy tagProxy = store.add(slotNumber, technologies);

            Intent intent = isoDepTagFactory.getTag(tagProxy.getHandle(), null, uid, true, historicalBytes, tagService, extras);

            if(LOGGER.isDebugEnabled()) LOGGER.debug("Broadcast hce");

            context.sendBroadcast(intent, ANDROID_PERMISSION_NFC);

            return tagProxy;
        } catch (Exception e) {
            if(LOGGER.isInfoEnabled()) LOGGER.info("Problem reading from tag", e);

            broadcast(ExternalNfcTagCallback.ACTION_TECH_DISCOVERED);
        }
        return null;
    }

    public TagProxy card(int slotNumber, AbstractReaderIsoDepWrapper wrapper, byte[] uid, byte[] historicalBytes, IntentEnricher extras) {
        try {
            List<TagTechnology> technologies = new ArrayList<>();

            technologies.add(new WrapNfcACommandTechnology(wrapper, transceiveResultExceptionMapper, false));
            technologies.add(new IsoDepCommandTechnology(wrapper, false, transceiveResultExceptionMapper));

            TagProxy tagProxy = store.add(slotNumber, technologies);

            Intent intent = isoDepTagFactory.getTag(tagProxy.getHandle(), null, uid, false, historicalBytes, tagService, extras);

            wrapper.setTagProxy(tagProxy);

            if(LOGGER.isDebugEnabled()) LOGGER.debug("Broadcast IsoDep tag");

            context.sendBroadcast(intent, ANDROID_PERMISSION_NFC);

            return tagProxy;
        } catch (Exception e) {
            if(LOGGER.isInfoEnabled()) LOGGER.info("Problem reading from tag", e);

            broadcast(ExternalNfcTagCallback.ACTION_TECH_DISCOVERED);
        }
        return null;
    }

}
