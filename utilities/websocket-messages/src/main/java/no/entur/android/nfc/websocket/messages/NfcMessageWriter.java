package no.entur.android.nfc.websocket.messages;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class NfcMessageWriter {

    public byte[] write(NfcMessage message) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(out);

            dout.writeInt(NfcMessageReader.VERSION);

            dout.writeInt(message.getType());

            message.write(dout);

            byte[] response = out.toByteArray();

            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
