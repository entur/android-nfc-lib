package no.entur.android.nfc.external.atr210.schema;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReaderStatus {

    private String cardATR;
    private String id;

    @JsonDeserialize(using = StatusDeserializer.class)
    private List<Status> status = new ArrayList<>();

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCardATR() {
        return cardATR;
    }

    public void setCardATR(String cardATR) {
        this.cardATR = cardATR;
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

    public boolean hasStatus(Status status) {
        return this.status.contains(status);
    }
}
