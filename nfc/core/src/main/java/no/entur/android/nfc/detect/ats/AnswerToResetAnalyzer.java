package no.entur.android.nfc.detect.ats;

import android.content.Intent;

import no.entur.android.nfc.wrapper.Tag;
import no.entur.android.nfc.wrapper.tech.IsoDep;

/**
 * ATR / historical bytes
 *
 */

public interface AnswerToResetAnalyzer {

    /**
     *
     *
     * @param isoDep
     * @param tag
     * @param intent
     * @return null if card is guaranteed to not match
     */

    AnswerToResetAnalyzeResult processAnswerToReset(IsoDep isoDep, Tag tag, Intent intent);
}
