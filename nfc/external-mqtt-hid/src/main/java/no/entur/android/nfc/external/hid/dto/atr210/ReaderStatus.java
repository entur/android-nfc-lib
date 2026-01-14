package no.entur.android.nfc.external.hid.dto.atr210;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import no.entur.android.nfc.external.hid.intent.NfcCardStatus;

@JsonIgnoreProperties(ignoreUnknown = true, value = {"stability"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReaderStatus {

    private String cardCSN;
    private String cardATR;
    private String id;

    @JsonDeserialize(using = StatusDeserializer.class)
    @JsonSerialize(using = StatusSerializer.class)
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

    public void add(NfcCardStatus ... statuses) {
        for (NfcCardStatus nfcCardStatus : statuses) {
            this.status.add(nfcCardStatus);
        }
    }

    public boolean hasStatus(NfcCardStatus status) {
        return this.status.contains(status);
    }

    public boolean hasCardAtr() {
        return cardATR != null && !cardATR.isEmpty();
    }

    public void setCardCSN(String cardCSN) {
        this.cardCSN = cardCSN;
    }

    public boolean hasCardCsn() {
        return cardCSN != null && !cardCSN.isEmpty();
    }

    public String getCardCSN() {
        return cardCSN;
    }
}
