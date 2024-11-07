package no.entur.android.nfc.detect.technology;

import android.content.Intent;

import no.entur.android.nfc.detect.TagTechnologies;
import no.entur.android.nfc.wrapper.Tag;

/**
 * ATR / historical bytes
 *
 * @see <a href="https://smartcard-atr.apdu.fr/">smartcard-atr.apdu.fr</a>
 */

public interface TechnologyAnalyzer {

    /**
     * Process technology types (isoDep, mifare ultralight etc) to see whether they
     * match the required nature.
     *
     * @param tagTechnologies parsed technologies
     * @param tag
     * @param intent
     * @return null if card is guaranteed to not match
     */

    TechnologyAnalyzeResult processTechnology(TagTechnologies tagTechnologies, Tag tag, Intent intent);

    /**
     * Return the in-scope technologies considered by this implementation
     *
     * @return
     */

    String[] getTechnologies();
}
