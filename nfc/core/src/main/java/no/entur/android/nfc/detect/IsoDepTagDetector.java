package no.entur.android.nfc.detect;

import no.entur.android.nfc.detect.app.SelectApplicationAnalyzer;
import no.entur.android.nfc.detect.ats.AnswerToResetAnalyzer;
import no.entur.android.nfc.detect.tag.UidAnalyzer;

public interface IsoDepTagDetector extends AnswerToResetAnalyzer, SelectApplicationAnalyzer, UidAnalyzer, Comparable<IsoDepTagDetector> {
}
