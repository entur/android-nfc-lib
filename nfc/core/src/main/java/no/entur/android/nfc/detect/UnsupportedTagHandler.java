package no.entur.android.nfc.detect;

import android.content.Intent;

import no.entur.android.nfc.wrapper.Tag;

public interface UnsupportedTagHandler {

    void onUnsupportedTagDiscovered(Tag tag, Intent intent);

}
