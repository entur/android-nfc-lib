package no.entur.android.nfc.detect;

import no.entur.android.nfc.detect.app.SelectApplicationAnalyzer;
import no.entur.android.nfc.detect.ats.AnswerToResetAnalyzer;
import no.entur.android.nfc.detect.tag.TagIdAnalyzer;

public interface IsoDepTagDetector extends AnswerToResetAnalyzer, SelectApplicationAnalyzer, TagIdAnalyzer, Comparable<IsoDepTagDetector> {
}
