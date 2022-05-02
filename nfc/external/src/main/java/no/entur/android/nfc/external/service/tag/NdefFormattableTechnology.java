package no.entur.android.nfc.external.service.tag;

import no.entur.android.nfc.wrapper.tech.TagTechnology;

public interface NdefFormattableTechnology extends TagTechnology {

    int formatNdef();

}
