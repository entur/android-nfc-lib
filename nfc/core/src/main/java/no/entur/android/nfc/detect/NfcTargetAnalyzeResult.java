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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TechnologyAnalyzeResult getTechnologyAnalyzeResult() {
        return technologyAnalyzeResult;
    }

    public void setTechnologyAnalyzeResult(TechnologyAnalyzeResult technologyAnalyzeResult) {
        this.technologyAnalyzeResult = technologyAnalyzeResult;
    }

    public UidAnalyzeResult getUidAnalyzeResult() {
        return uidAnalyzeResult;
    }

    public void setUidAnalyzeResult(UidAnalyzeResult uidAnalyzeResult) {
        this.uidAnalyzeResult = uidAnalyzeResult;
    }

    public SelectApplicationAnalyzeResult getSelectApplicationAnalyzeResult() {
        return selectApplicationAnalyzeResult;
    }

    public void setSelectApplicationAnalyzeResult(SelectApplicationAnalyzeResult selectApplicationAnalyzeResult) {
        this.selectApplicationAnalyzeResult = selectApplicationAnalyzeResult;
    }
}
