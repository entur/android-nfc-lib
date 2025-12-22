package no.entur.android.nfc.external.hid.intent;

import java.util.List;

public class NfcHfReader {

    private String id;

    private List<NfcCardStatus> status;

    private String name;

    public NfcHfReader(String id, List<NfcCardStatus> status, String name) {
        this.id = id;
        this.status = status;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public List<NfcCardStatus> getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }
}
