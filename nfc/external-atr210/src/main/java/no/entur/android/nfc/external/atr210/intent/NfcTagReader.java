package no.entur.android.nfc.external.atr210.intent;

import java.util.ArrayList;
import java.util.List;

import no.entur.android.nfc.external.atr210.schema.Status;

public class NfcTagReader {

    private String id;

    private List<Status> status = new ArrayList<>();

    private String name;

    public NfcTagReader(String id, List<Status> status, String name) {
        this.id = id;
        this.status = status;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public List<Status> getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }
}
