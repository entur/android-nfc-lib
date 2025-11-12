package no.entur.android.nfc.wrapper.test.tech.transceive;

import androidx.core.util.Predicate;

public class ByteArrayPredicate implements Predicate<byte[]> {

    private final byte[] data;

    public ByteArrayPredicate(byte[] data) {
        this.data = data;
    }

    @Override
    public boolean test(byte[] command) {
        if(data.length != command.length) {
            return false;
        }
        for(int i = 0; i < command.length; i++) {
            if(command[i] != data[i]) {
                return false;
            }
        }
        return true;
    }

}
