package no.entur.android.nfc.detect.tag;

import android.content.Intent;

import no.entur.android.nfc.wrapper.Tag;

public interface TagIdAnalyzer {

    /**
     *
     * @param tag
     * @param intent
     * @return null if card is guaranteed to not match
     *
     */
    TagIdAnalyzeResult processTagId(Tag tag, Intent intent);

}
