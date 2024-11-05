package no.entur.android.nfc.detect;

import android.content.Intent;

import no.entur.android.nfc.wrapper.ReaderCallback;
import no.entur.android.nfc.wrapper.Tag;
import no.entur.android.nfc.wrapper.tech.IsoDep;
import no.entur.android.nfc.wrapper.tech.MifareUltralight;

public class TagHandler implements ReaderCallback {

    private IsoDepHandler isoDepHandler;
    private MifareUltralightHandler mifareUltralightHandler;
    private UnsupportedTagHandler unsupportedTagHandler;

    @Override
    public void onTagDiscovered(Tag tag, Intent intent) {

        String[] techList = tag.getTechList();
        for (String tech : techList) {
            if(tech.equals(IsoDep.class.getName())) {
                IsoDep isoDep = IsoDep.get(tag);

                isoDepHandler.onIsoDepDiscovered(isoDep, tag, intent);

                return;
            } else if(tech.equals(MifareUltralight.class.getName())) {
                MifareUltralight mifareUltralight = MifareUltralight.get(tag);

                mifareUltralightHandler.onMifareUltralightDiscovered(mifareUltralight, tag, intent);

                return;
            }
        }
        unsupportedTagHandler.onUnsupportedTagDiscovered(tag, intent);
    }
}
