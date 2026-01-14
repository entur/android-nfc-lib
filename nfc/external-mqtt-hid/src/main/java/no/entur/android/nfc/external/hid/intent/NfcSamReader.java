package no.entur.android.nfc.external.hid.intent;

import java.util.List;

public class NfcSamReader {

    private String id;

    private List<NfcCardStatus> status;

    private String name;

    public NfcSamReader(String id, List<NfcCardStatus> status, String name) {
        this.id = id;
        this.status = status;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<NfcCardStatus> getStatus() {
        return status;
    }

    public void setStatus(List<NfcCardStatus> status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
