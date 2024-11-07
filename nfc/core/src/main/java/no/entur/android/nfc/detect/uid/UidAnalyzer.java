package no.entur.android.nfc.detect.uid;

import android.content.Intent;

import no.entur.android.nfc.detect.TagTechnologies;
import no.entur.android.nfc.wrapper.Tag;

/**
 * UID analyzer. Notably, UIDs for tags and HCE device seem to differ in length.
 *
 * @see <a href="https://gototags.com/nfc/chip/features/uid">UID</a>
 */

public interface UidAnalyzer {

    /**
     *
     * @param tag tag
     * @param intent intent (if present)
     * @return result
     *
     */
    UidAnalyzeResult processUid(TagTechnologies tagTechnologies, Tag tag, Intent intent);

}
