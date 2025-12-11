package no.entur.android.nfc.external.hwb.schema;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReaderStatus {

    private String cardATR;
    private String id;

    // UNAWARE, IGNORE, CHANGED,
    //UNKNOWN, UNAVAILABLE,
    //EMPTY, PRESENT, EXCLUSIVE,
    //INUSE, MUTE, UNPOWERED
    private String status;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
