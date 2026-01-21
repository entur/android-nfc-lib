package no.entur.android.nfc.external.hid.card;

import android.os.Parcelable;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import no.entur.android.nfc.ResponseAPDU;
import no.entur.android.nfc.external.tag.AbstractReaderIsoDepWrapper;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;

public class Atr210DesfireWrapper extends AbstractReaderIsoDepWrapper {

    /* Status codes */
    public static final byte OPERATION_OK = (byte) 0x00;
    public static final byte ADDITIONAL_FRAME = (byte) 0xAF;
    public static final byte STATUS_OK = (byte) 0x91;

    public static final int MAX_CAPDU_SIZE = 55;
    public static final int MAX_RAPDU_SIZE = 60;

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

        // is this already an APDU?
        int cls = data.length > 0 ? data[0] & 0xFF : -1;

        if (data.length >= 4 && (cls == 0x00 || cls == 0x90 || cls == 0xFF) && isApduLength(data)) {
            Log.i(getClass().getName(), " => " + ByteArrayHexStringConverter.toHexString(data));
            byte[] transcieve = cardCommands.transcieve(data);
            Log.i(getClass().getName(), " <= " + ByteArrayHexStringConverter.toHexString(transcieve));

            return transcieve;
        }

        Log.i(getClass().getName(), " -> " + ByteArrayHexStringConverter.toHexString(data));

        // wrapping the command in an ADPU might bump into max command/response length
        // so wrap both command and response with "additional frame" status
        // this is not optimal for all cases, but the only way to support native desfire commands

        byte[] transcieve;
        if(cls == 0x0A) { // AUTHENTICATE
            // do not handle AF
            transcieve = transmitRaw(data);
        } else {
            transcieve = transmitChain(data);
        }

        Log.i(getClass().getName(), " <- " + ByteArrayHexStringConverter.toHexString(transcieve));

        return transcieve;
    }

    private boolean isApduLength(byte[] data) {
        if (data.length >= 5) {

            // CLA (Class): 1 byte.
            // INS (Instruction): 1 byte.
            // P1 (Parameter 1): 1 byte.
            // P2 (Parameter 2): 1 byte.
            // Lc (Length of Command Data): Optional (1 or 3 bytes).
            // Data Field: Optional, length specified by Lc.
            // Le (Length Expected): Optional (1 or 3 bytes).

            int lengthOfCommandData = data[4] & 0xFF;

            // no Length expected
            if (lengthOfCommandData == data.length - 5) {
                return true;
            }
            // 1 byte Length expected
            if (lengthOfCommandData + 1 == data.length - 5) {
                return true;
            }
            // 3 byte Length expected
            if (lengthOfCommandData + 3 == data.length - 5) {
                return true;
            }

            return false;
        }
        return true;
    }

    public byte[] transceiveRaw(byte[] req) throws Exception {
        return cardCommands.transcieve(req);
    }

    @Override
    public Parcelable transceive(Parcelable parcelable) throws Exception {
        return cardCommands.transcieve(parcelable);
    }

    /**
     * Send compressed command message
     *
     * @param adpu raw adpu
     * @return raw response
     * @throws Exception
     */

    public byte[] transmitRaw(byte[] adpu) throws IOException {
        return responseADPUToRaw(rawToRequestADPU(adpu));
    }

    public static byte[] responseADPUToRaw(byte[] response) {

        byte[] result = new byte[response.length - 1];
        result[0] = response[response.length - 1];

        System.arraycopy(response, 0, result, 1, response.length - 2);

        return result;
    }

    public byte[] rawToRequestADPU(byte[] commandMessage) throws IOException {
        return transceiveImpl(wrapMessage(commandMessage[0], commandMessage, 1, commandMessage.length - 1));
    }

    public static byte[] wrapMessage(byte command) {
        return new byte[]{(byte) 0x90, command, 0x00, 0x00, 0x00};
    }

    private static byte[] wrapCommand(byte command, byte[] parameters) {
        return wrapCommand(command, parameters, 0, parameters.length);
    }

    private static byte[] wrapCommand(byte command, byte[] parameters, int offset, int length) {
        byte[] stream;
        if (parameters != null && length > 0) {
            stream = new byte[5 + 1 + length];
        } else {
            stream = new byte[5];
        }
        stream[0] = (byte) 0x90;
        stream[1] = command;

        if (parameters != null && parameters.length > 0) {
            stream[4] = (byte) length;
            System.arraycopy(parameters, offset, stream, 5, length);
        }

        return stream;
    }

    public static byte[] wrapMessage(byte command, byte[] parameters, int offset, int length) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        stream.write((byte) 0x90);
        stream.write(command);
        stream.write((byte) 0x00);
        stream.write((byte) 0x00);
        if (parameters != null && length > 0) {
            // actually no length if empty length
            stream.write(length);
            stream.write(parameters, offset, length);
        }
        stream.write((byte) 0x00);

        return stream.toByteArray();
    }

    /**
     * Send a command to the card and return the response.
     *
     * @param command the command
     * @return the PICC response
     * @throws Exception
     */
    public byte[] transceiveImpl(byte[] command) throws IOException {

        Log.i(getClass().getName(), " -> " + ByteArrayHexStringConverter.toHexString(command));
        byte[] response = cardCommands.transcieve(command);
        Log.i(getClass().getName(), " <- " + ByteArrayHexStringConverter.toHexString(response));

        return response;
    }

    public static String getHexString(byte[] a, boolean space) {
        StringBuilder sb = new StringBuilder();
        for (byte b : a) {
            sb.append(String.format("%02x", b & 0xff));
            if (space) {
                sb.append(' ');
            }
        }
        return sb.toString().trim().toUpperCase();
    }

    public byte[] transmitChain(byte[] adpu) throws IOException {
        return receieveResponseChain(sendRequestChain(adpu));
    }

    public byte[] receieveResponseChain(byte[] response) throws IOException {

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        output.write(0x00);
        do {

            output.write(response, 0, response.length - 2);

            if (response[response.length - 1] != ADDITIONAL_FRAME) {
                byte[] result = output.toByteArray();

                result[0] = response[response.length - 1];

                return result;
            }

            response = transceiveImpl(wrapMessage(ADDITIONAL_FRAME));
        } while (true);
    }

    public byte[] sendRequestChain(byte[] commandMessage) throws IOException {

        int parts = 0;

        int offset = 1; // data area of apdu

        byte nextCommand = commandMessage[0];
        while (true) {
            int nextLength = Math.min(MAX_CAPDU_SIZE - 1, commandMessage.length - offset);

            byte[] request = wrapMessage(nextCommand, commandMessage, offset, nextLength);

            parts++;
            byte[] response = transceive(request);
            if (response[response.length - 2] != STATUS_OK) {
                if(parts > 1) {
                    Log.i(getClass().getName(), "Command completed in " + parts + " parts");
                }
                return response;
            }

            offset += nextLength;
            if (offset == commandMessage.length) {
                if(parts > 1) {
                    Log.i(getClass().getName(), "Command completed in " + parts + " parts");
                }
                return response;
            }

            if (response.length != 2) {
                throw new IllegalArgumentException("Expected empty response payload while transmitting request");
            }
            byte status = response[response.length - 1];
            if (status != ADDITIONAL_FRAME) {
                throw new IOException("Unexpected error while transferring command: " + Integer.toHexString(status & 0xFF));
            }
            nextCommand = ADDITIONAL_FRAME;
        }
    }
}
