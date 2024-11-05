package no.entur.android.nfc.detect.app;

import android.content.Intent;

import no.entur.android.nfc.wrapper.Tag;
import no.entur.android.nfc.wrapper.tech.IsoDep;

public interface SelectApplicationAnalyzer {

    SelectApplicationAnalyzeResult processApplication(IsoDep isoDep, Tag tag, Intent intent);

}
