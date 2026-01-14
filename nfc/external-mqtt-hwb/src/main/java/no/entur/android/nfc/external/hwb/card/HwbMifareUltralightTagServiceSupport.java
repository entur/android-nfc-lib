package no.entur.android.nfc.external.hwb.card;

import android.content.Context;
import android.content.Intent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

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

public class HwbMifareUltralightTagServiceSupport extends AbstractTagServiceSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(HwbMifareUltralightTagServiceSupport.class);

    protected MifareUltralightTagFactory mifareUltralightTagFactory = new MifareUltralightTagFactory();

    protected TransceiveResultExceptionMapper exceptionMapper;

    public HwbMifareUltralightTagServiceSupport(Context context, INfcTag tagService, TagProxyStore store, TransceiveResultExceptionMapper exceptionMapper) {
        super(context, tagService, store);

        this.exceptionMapper = exceptionMapper;
    }

    public TagProxy mifareUltralight(int slotNumber, AbstractReaderIsoDepWrapper wrapper, byte[] atr, byte[] uid, byte[] historicalBytes, IntentEnricher extras) {
        List<TagTechnology> technologies = new ArrayList<>();
        technologies.add(new NfcADefaultCommandTechnology(wrapper, false, exceptionMapper));
        technologies.add(new HwbMifareUltralightCommandTechnology(wrapper, exceptionMapper));

        TagProxy tagProxy = store.add(slotNumber, technologies);

        Intent intent = mifareUltralightTagFactory.getTag(tagProxy.getHandle(), slotNumber, MifareUltralightTagFactory.TYPE_ULTRALIGHT, uid, atr, tagService, extras);

        LOGGER.debug("Broadcast mifare ultralight");

        context.sendBroadcast(intent, ANDROID_PERMISSION_NFC);

        return tagProxy;
    }


}
