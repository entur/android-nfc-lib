package no.entur.android.nfc.external.hid.dto.atr210;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.ArrayList;
import java.util.List;

import no.entur.android.nfc.external.hid.intent.NfcCardStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReaderStatus {

    private String cardATR;
    private String id;

    @JsonDeserialize(using = StatusDeserializer.class)
    private List<NfcCardStatus> status = new ArrayList<>();

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

    public List<NfcCardStatus> getStatus() {
        return status;
    }

    public void setStatus(List<NfcCardStatus> status) {
        this.status = status;
    }

    public boolean hasStatus(NfcCardStatus status) {
        return this.status.contains(status);
    }
}
