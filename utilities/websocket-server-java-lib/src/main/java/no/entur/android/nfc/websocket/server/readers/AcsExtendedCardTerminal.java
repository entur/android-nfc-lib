package no.entur.android.nfc.websocket.server.readers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;

import no.entur.android.nfc.websocket.server.ByteArrayHexStringConverter;
import no.entur.android.nfc.websocket.server.ExtendedCardTerminal;

public abstract class AcsExtendedCardTerminal extends ExtendedCardTerminal {

    private final static Logger LOGGER = LoggerFactory.getLogger(AcsExtendedCardTerminal.class);

    protected static final int SCARD_CTL_CODE = 0x310000 + 3500 * 4;

    public AcsExtendedCardTerminal(CardTerminal delegate) {
        super(delegate);
    }

    public abstract String getFirmware() throws CardException;

    public boolean setPICC(int picc) throws CardException {
        LOGGER.info("Set PICC " + Integer.toHexString(picc));

        if ((picc & 0xFF) != picc) {
            throw new RuntimeException("Unexpected PICC " + Integer.toHexString(picc));
        }

        Card ca = delegate.connect("DIRECT");

        LOGGER.debug("Get exclusive lock");
        ca.beginExclusive();
        LOGGER.debug("Got exclusive lock");
        try {
            byte[] pseudo = new byte[] { (byte) 0xFF, 0x00, 0x51, (byte) picc, 0x00 };

            byte[] bytes = ca.transmitControlCommand(SCARD_CTL_CODE, pseudo);

            if (!isSuccessControl(bytes)) {
                LOGGER.debug("Unable to set PICC: " + Integer.toHexString(picc));

                throw new AcsCardException("Card responded with error code");
            }

            final int operation = bytes[1] & 0xFF;

            if (operation != picc) {
                LOGGER.warn("Unable to properly update PICC: Expected " + Integer.toHexString(picc) + " got " + Integer.toHexString(operation));

                return false;
            } else {
                LOGGER.debug("Updated PICC " + Integer.toHexString(operation) + " (" + Integer.toHexString(picc) + ")");

                return true;
            }
        } finally {
            ca.endExclusive();
            LOGGER.debug("Released exclusive lock");
        }

    }

    public static boolean isSuccessControl(byte[] in) {
        return (in[in.length - 2] & 0xFF) == 0x90;
    }

    public static boolean isSuccess(CommandAPDU response) {
        return response.getCLA() == 0xE1 && response.getP1() == 0x00 && response.getP2() == 0x00 && response.getINS() == 0x00;
    }

    public boolean setAutomaticPICCPolling(int value) throws CardException {
        LOGGER.info("Set automatic polling " + Integer.toHexString(value));
        CommandAPDU command = new CommandAPDU(0xE0, 0x00, 0x00, 0x23, new byte[] { (byte)value});

        Card ca = delegate.connect("DIRECT");

        LOGGER.debug("Get exclusive lock");
        ca.beginExclusive();
        LOGGER.debug("Got exclusive lock");
        try {
            byte[] response = ca.transmitControlCommand(SCARD_CTL_CODE, command.getBytes());

            CommandAPDU responseAPDU = new CommandAPDU(response);
            if (!isSuccess(responseAPDU)) {
                throw new AcsCardException("Card responded with error " + ByteArrayHexStringConverter.toHexString(response));
            }

            final int operation = responseAPDU.getBytes()[0] & 0xFF;

            if (operation != value) {
                LOGGER.warn("Unable to properly update automatic polling: Expected " + Integer.toHexString(value) + " got " + Integer.toHexString(operation));

                return false;
            } else {
                LOGGER.debug("Updated automatic polling " + Integer.toHexString(operation) + " (" + Integer.toHexString(value) + ")");
            }
            return true;
        } finally {
            ca.endExclusive();
            LOGGER.debug("Released exclusive lock");
        }
    }

    @Override
    public List<String> identifyTechnologies(Card card, CardChannel channel) {
        ATR atr = card.getATR();

        byte[] atrBytes = atr.getBytes();

        LOGGER.info("Got ATR " + ByteArrayHexStringConverter.toHexString(atrBytes));

        if(atrBytes.length == 6) {

            if(atrBytes[0] == (byte)0x3B &&
                    atrBytes[1] == (byte)0x81 &&
                    atrBytes[2] == (byte)0x80 &&
                    atrBytes[3] == (byte)0x01 &&
                    atrBytes[4] == (byte)0x80 &&
                    atrBytes[5] == (byte)0x80
            ) {
                return Arrays.asList("IsoDep");
            }
        }

        return Collections.emptyList();
    }
}
