package no.entur.android.nfc.external.tag.bulk;



public class TransceiveResponse {

    private int id;
    private byte[] frame;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte[] getFrame() {
        return frame;
    }

    public void setFrame(byte[] frame) {
        this.frame = frame;
    }
}
