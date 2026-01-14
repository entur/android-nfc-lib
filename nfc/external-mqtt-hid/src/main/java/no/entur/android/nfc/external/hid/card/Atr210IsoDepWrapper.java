package no.entur.android.nfc.external.hid.card;

import android.os.Parcelable;
import android.util.Log;

import java.io.IOException;

import no.entur.android.nfc.CommandAPDU;
import no.entur.android.nfc.external.tag.AbstractReaderIsoDepWrapper;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;

public class Atr210IsoDepWrapper extends AbstractReaderIsoDepWrapper {

	private Atr210CardCommands cardCommands;
    private boolean desfire = false;

	public Atr210IsoDepWrapper(Atr210CardCommands cardCommands) {
		super(-1);
		this.cardCommands = cardCommands;
	}

	public byte[] transceive(byte[] data) throws IOException {

        CommandAPDU apdu = new CommandAPDU(0xFF, 0x00, 0x00, 0x00);

        byte[] getMediaType = apdu.getBytes();

        Log.i(getClass().getName(), " -> " + ByteArrayHexStringConverter.toHexString(data) + " -> " + ByteArrayHexStringConverter.toHexString(getMediaType));

        byte[] r = cardCommands.transcieve(getMediaType);

        Log.i(getClass().getName(), " <- " + ByteArrayHexStringConverter.toHexString(r));


        if(desfire) {
            byte[] raw = new byte[6 + data.length];

            raw[0] = (byte) 0xFF;
            raw[1] = 0x28;
            raw[2] = 0;
            raw[3] = 0x00;
            raw[4] = (byte) (data.length + 1);
            raw[5] = (byte) 0xF0;

            System.arraycopy(data, 0, raw, 6, data.length);

            byte[] dataPlus1 = new byte[data.length + 1];

            //raw = ByteArrayHexStringConverter.hexStringToByteArray("FF28000003F03000");

            byte[] transcieve = cardCommands.transcieve(raw);

            if(transcieve.length < 2) {
                throw new IOException("Expected response size >= 2");
            }

            int sw1 =  transcieve[transcieve.length - 2] & 0xFF;
            int sw2 =  transcieve[transcieve.length - 1] & 0xFF;

            byte[] rawResponse;
            if(sw1 == 0x90 && sw2 == 0x00) {
                rawResponse = new byte[transcieve.length - 3];
                System.arraycopy(transcieve, 3, rawResponse, 0 , rawResponse.length);
            } else {
                rawResponse = new byte[]{transcieve[transcieve.length - 1], transcieve[transcieve.length - 2]};
            }

            Log.i(getClass().getName(), " <- " + ByteArrayHexStringConverter.toHexString(transcieve) + " <- " + ByteArrayHexStringConverter.toHexString(rawResponse) );

            return rawResponse;
/*

           byte[] raw = new byte[3 + data.length];
           System.arraycopy(data, 0, raw, 3, data.length);
           raw[0] = 0x00;
           raw[1] = 0x28;
           raw[2] = (byte) 0xF0;

            Log.i(getClass().getName(), " -> " + ByteArrayHexStringConverter.toHexString(data) + " -> " + ByteArrayHexStringConverter.toHexString(raw));
            byte[] transcieve = cardCommands.transcieve(raw);

            if(transcieve.length < 2) {
                throw new IOException("Expected response size >= 2");
            }

            int sw1 =  transcieve[transcieve.length - 2] & 0xFF;
            int sw2 =  transcieve[transcieve.length - 1] & 0xFF;

            byte[] rawResponse;
            if(sw1 == 0x90 && sw2 == 0x00) {
                rawResponse = new byte[transcieve.length - 3];
                System.arraycopy(transcieve, 3, rawResponse, 0 , rawResponse.length);
            } else {
                rawResponse = new byte[]{transcieve[transcieve.length - 1], transcieve[transcieve.length - 2]};
            }

            Log.i(getClass().getName(), " <- " + ByteArrayHexStringConverter.toHexString(transcieve) + " <- " + ByteArrayHexStringConverter.toHexString(rawResponse) );

            return rawResponse;
*/
        } else {
            Log.i(getClass().getName(), " -> " + ByteArrayHexStringConverter.toHexString(data));
            byte[] transcieve = cardCommands.transcieve(data);
            Log.i(getClass().getName(), " <- " + ByteArrayHexStringConverter.toHexString(transcieve));
            return transcieve;
        }
    }

	public byte[] transceiveRaw(byte[] req) throws Exception {
		return cardCommands.transcieve(req);
	}

    @Override
    public Parcelable transceive(Parcelable parcelable) throws Exception {
        return cardCommands.transcieve(parcelable);
    }

    public void setDesfire(boolean desfire) {
        this.desfire = desfire;
    }
}
