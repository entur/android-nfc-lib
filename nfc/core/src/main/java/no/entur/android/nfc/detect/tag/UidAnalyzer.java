package no.entur.android.nfc.detect.tag;

import android.content.Intent;

import no.entur.android.nfc.wrapper.Tag;

public interface UidAnalyzer {

    /**
     *
     * @param tag
     * @param intent
     * @return null if card is guaranteed to not match
     *
     */
    UidAnalyzeResult processTagId(Tag tag, Intent intent);

}
