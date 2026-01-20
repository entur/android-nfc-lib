package no.entur.android.nfc.external.hid.card;

import android.os.Parcelable;
import android.util.Log;

import java.io.IOException;

import no.entur.android.nfc.ResponseAPDU;
import no.entur.android.nfc.external.tag.AbstractReaderIsoDepWrapper;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;

public class Atr210DesfireWrapper extends AbstractReaderIsoDepWrapper {

	private Atr210CardCommands cardCommands;

	public Atr210DesfireWrapper(Atr210CardCommands cardCommands) {
		super(-1);
		this.cardCommands = cardCommands;
	}

	public byte[] transceive(byte[] data) throws IOException {

     /* Native DESFire APDUs will be wrapped in ISO7816-4 APDUs:
	   CAPDUs will be 5 bytes longer (CLA+P1+P2+Lc+Le)
	   RAPDUs will be 1 byte longer  (SW1 SW2 instead of 1 status byte)
	 */

        byte[] commandAdpu = new byte[6 + data.length - 1];
        commandAdpu[0] = (byte) 0x90;
        commandAdpu[1] = data[0];
        commandAdpu[4] = (byte)(data.length - 1);

        System.arraycopy(data, 1, commandAdpu, 5, data.length - 1);

        Log.i(getClass().getName(), " -> " +  ByteArrayHexStringConverter.toHexString(data));
        Log.i(getClass().getName(), " -> " +  ByteArrayHexStringConverter.toHexString(commandAdpu));
        byte[] transcieve = cardCommands.transcieve(commandAdpu);
        Log.i(getClass().getName(), " <- " +  ByteArrayHexStringConverter.toHexString(transcieve));

        ResponseAPDU responseAPDU = new ResponseAPDU(transcieve);

        byte[] data1 = responseAPDU.getData();

        byte[] res = new byte[data1.length + 1];
        res[0] = (byte) responseAPDU.getSW2();
        System.arraycopy(data1, 0, res, 1, data1.length);

        Log.i(getClass().getName(), " <- " +  ByteArrayHexStringConverter.toHexString(res));

        return res;
    }

	public byte[] transceiveRaw(byte[] req) throws Exception {
		return cardCommands.transcieve(req);
	}

    @Override
    public Parcelable transceive(Parcelable parcelable) throws Exception {
        return cardCommands.transcieve(parcelable);
    }

}
