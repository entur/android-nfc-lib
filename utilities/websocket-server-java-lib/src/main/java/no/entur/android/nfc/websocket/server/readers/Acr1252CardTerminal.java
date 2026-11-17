package no.entur.android.nfc.websocket.server.readers;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

/**
 * ACS ACR1252U.
 */

public class Acr1252CardTerminal extends AcsExtendedCardTerminal {

    // ldd -r /usr/bin/pcsc_scan
    // https://stackoverflow.com/questions/12376257/accessing-javax-smartcardio-from-linux-64-bits
    // https://ludovicrousseau.blogspot.com/2021/08/pcsc-lite-configuration-using.html
    // https://stackoverflow.com/questions/35389657/how-to-send-commands-to-smart-card-reader-and-not-to-the-smart-card-while-no-c
    // https://stackoverflow.com/questions/41851527/unkown-error-0x16-on-smartcard-reader-access
    // https://stackoverflow.com/questions/31131569/unable-to-claim-usb-interface-device-or-resource-busy
    // https://github.com/intarsys/smartcard-io

    public Acr1252CardTerminal(CardTerminal delegate) {
        super(delegate);
    }

    @Override
    public String getFirmware() throws CardException {
        return getFirmwareUsingPseudoApdu();
    }

    // verified on an ACR1252U, FW ACR1252U_V111.1; card commands work normally after the session
    @Override
    protected boolean isTransparentSessionSupported() {
        return true;
    }

    // verified on an ACR1252U, FW ACR1252U_V111.1: native DESFire SelectApplication returns 00 90 00
    @Override
    public boolean isStatusWordAppendedToSingleByteResponses() {
        return true;
    }

    @Override
    public void startPolling() throws CardException {
        setPICC(POLL_ISO14443_TYPE_A | POLL_ISO14443_TYPE_B);
        setAutomaticPICCPolling(0b11000001);
    }

    @Override
    public void stopPolling() throws CardException {
        setAutomaticPICCPolling(0b00000000);
    }
}
