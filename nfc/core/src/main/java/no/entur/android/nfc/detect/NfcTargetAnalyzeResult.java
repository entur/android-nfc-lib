package no.entur.android.nfc.detect;

import no.entur.android.nfc.detect.app.SelectApplicationAnalyzeResult;
import no.entur.android.nfc.detect.uid.UidAnalyzeResult;
import no.entur.android.nfc.detect.technology.TechnologyAnalyzeResult;

public class NfcTargetAnalyzeResult {

    private String id;

    private TechnologyAnalyzeResult technologyAnalyzeResult;

    private UidAnalyzeResult uidAnalyzeResult;

    private SelectApplicationAnalyzeResult selectApplicationAnalyzeResult;

    // TODO: Include command/response history?

}
