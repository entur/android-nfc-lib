package no.entur.android.nfc.wrapper.tech.utils.bulk;

public class BulkTransceiveResponse {

    private int id;
    private byte[] response;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte[] getResponse() {
        return response;
    }

    public void setResponse(byte[] response) {
        this.response = response;
    }
}
