package no.entur.android.nfc.websocket.server.readers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.List;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

import no.entur.android.nfc.websocket.server.CardTerminalsPollingServer;

public class Acr1252CardTerminal extends AcsExtendedCardTerminal {

    private final static Logger LOGGER = LoggerFactory.getLogger(Acr1252CardTerminal.class);

    public Acr1252CardTerminal(CardTerminal delegate) {
        super(delegate);
    }

    @Override
    public String getFirmware() throws CardException {
        LOGGER.info("Read firmware for " + delegate.getName());

        Card ca = delegate.connect("DIRECT");

        // ldd -r /usr/bin/pcsc_scan
        // https://stackoverflow.com/questions/12376257/accessing-javax-smartcardio-from-linux-64-bits
        // https://ludovicrousseau.blogspot.com/2021/08/pcsc-lite-configuration-using.html
        // https://stackoverflow.com/questions/35389657/how-to-send-commands-to-smart-card-reader-and-not-to-the-smart-card-while-no-c
        // https://stackoverflow.com/questions/41851527/unkown-error-0x16-on-smartcard-reader-access
        // https://stackoverflow.com/questions/31131569/unable-to-claim-usb-interface-device-or-resource-busy
        // https://github.com/intarsys/smartcard-io

        LOGGER.debug("Get exclusive lock");
        ca.beginExclusive();
        LOGGER.debug("Got exclusive lock");
        try {
            // https://stackoverflow.com/questions/12265807/javax-smartcardio-transmit-to-nfc-usb-reader-without-card/12346874#12346874
            int SCARD_CTL_CODE = 0x310000 + 3500 * 4;

            byte[] pseudo = new byte[]{(byte) 0xFF, 0x00, 0x48, 0x00, 0x00};

            byte[] bytes = ca.transmitControlCommand(SCARD_CTL_CODE, pseudo);

            String firmware = new String(bytes, Charset.forName("ASCII"));

            LOGGER.info("Read firmware " + firmware);

            return firmware;
        } finally {
            ca.endExclusive();
            LOGGER.debug("Released exclusive lock");
        }
    }

    @Override
    public void startPolling() throws CardException {
        setPICC(0b00000011);
        setAutomaticPICCPolling(0b11000001);
    }

    @Override
    public void stopPolling() throws CardException {
        setAutomaticPICCPolling(0b00000000);
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public Card connect(String s) throws CardException {
        return delegate.connect(s);
    }

    @Override
    public boolean isCardPresent() throws CardException {
        return delegate.isCardPresent();
    }

    @Override
    public boolean waitForCardPresent(long l) throws CardException {
        return delegate.waitForCardPresent(l);
    }

    @Override
    public boolean waitForCardAbsent(long l) throws CardException {
        return delegate.waitForCardAbsent(l);
    }
}
