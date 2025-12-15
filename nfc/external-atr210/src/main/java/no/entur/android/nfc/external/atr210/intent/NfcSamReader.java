package no.entur.android.nfc.external.atr210.intent;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.ArrayList;
import java.util.List;

import no.entur.android.nfc.external.atr210.schema.Status;
import no.entur.android.nfc.external.atr210.schema.StatusDeserializer;

public class NfcSamReader {

    private String id;

    @JsonDeserialize(using = StatusDeserializer.class)
    private List<Status> status = new ArrayList<>();

    private String name;

    public NfcSamReader(String id, List<Status> status, String name) {
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

    public List<Status> getStatus() {
        return status;
    }

    public void setStatus(List<Status> status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
