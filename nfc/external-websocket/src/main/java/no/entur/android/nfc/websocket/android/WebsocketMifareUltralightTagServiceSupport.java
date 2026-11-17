package no.entur.android.nfc.websocket.android;

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
import no.entur.android.nfc.external.tag.AbstractReaderIsoDepWrapper;
import no.entur.android.nfc.external.tag.AbstractTagServiceSupport;
import no.entur.android.nfc.external.tag.IntentEnricher;
import no.entur.android.nfc.external.tag.MifareUltralightTagFactory;
import no.entur.android.nfc.external.tag.NfcADefaultCommandTechnology;
import no.entur.android.nfc.external.tag.TransceiveResultExceptionMapper;
import no.entur.android.nfc.wrapper.INfcTag;

public class WebsocketMifareUltralightTagServiceSupport extends AbstractTagServiceSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebsocketMifareUltralightTagServiceSupport.class);

    protected MifareUltralightTagFactory mifareUltralightTagFactory = new WebsocketMifareUltralightTagFactory();

    protected TransceiveResultExceptionMapper exceptionMapper;

    public WebsocketMifareUltralightTagServiceSupport(Context context, INfcTag tagService, TagProxyStore store, TransceiveResultExceptionMapper exceptionMapper) {
        super(context, tagService, store);

        this.exceptionMapper = exceptionMapper;
    }

    public TagProxy mifareUltralight(int slotNumber, AbstractReaderIsoDepWrapper wrapper, byte[] atr, byte[] uid, byte[] historicalBytes, IntentEnricher extras) {
        List<TagTechnology> technologies = new ArrayList<>();
        technologies.add(new NfcADefaultCommandTechnology(wrapper, false, exceptionMapper));
        technologies.add(new MifareUltralightDefaultCommandTechnology(wrapper, false, exceptionMapper));

        TagProxy tagProxy = store.add(slotNumber, technologies);

        Intent intent;
        try {
            intent = mifareUltralightTagFactory.getTag(tagProxy.getHandle(), slotNumber, MifareUltralightTagFactory.TYPE_ULTRALIGHT, uid, atr, tagService, extras);
        } catch (Exception e) {
            // i.e. not an NXP tag id; do not keep a tag which was never broadcast
            LOGGER.info("Problem creating mifare ultralight tag", e);

            store.remove(tagProxy);

            broadcast(ExternalNfcTagCallback.ACTION_TECH_DISCOVERED);

            return null;
        }

        LOGGER.debug("Broadcast mifare ultralight");

        context.sendBroadcast(intent, ANDROID_PERMISSION_NFC);

        return tagProxy;
    }


}
