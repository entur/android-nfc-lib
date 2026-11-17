package no.entur.android.nfc.websocket.server.readers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import javax.smartcardio.ATR;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

import no.entur.android.nfc.websocket.server.FakeCard;
import no.entur.android.nfc.websocket.server.FakeCardTerminal;

/**
 * Verify the reader commands sent by the ACS readers, against a fake reader which echoes the values it is set to.
 */

public class AcsCardTerminalsTest {

    private interface TerminalFactory {
        AcsExtendedCardTerminal create(CardTerminal delegate);
    }

    @Test
    public void testAcr122U() throws CardException {
        assertCommands(Acr122UCardTerminal::new, "ACR122U214",
                Arrays.asList("FF0051C300"),
                Arrays.asList("FF00514300"),
                Arrays.asList("FF00480000"));
    }

    @Test
    public void testAcr1251() throws CardException {
        assertCommands(Acr1251CardTerminal::new, "ACR1251U_V205",
                Arrays.asList("FF00510300"),
                Arrays.asList("FF00510000"),
                Arrays.asList("FF00480000"));
    }

    @Test
    public void testAcr1252() throws CardException {
        assertCommands(Acr1252CardTerminal::new, "ACR1252U_V302",
                Arrays.asList("FF00510300", "E000002301C1"),
                Arrays.asList("E00000230100"),
                Arrays.asList("FF00480000"));
    }

    @Test
    public void testAcr1222L() throws CardException {
        assertCommands(Acr1222LCardTerminal::new, "ACR1222L_V210",
                Arrays.asList("E00000200103"),
                Arrays.asList("E00000200100"),
                Arrays.asList("E000001800"));
    }

    @Test
    public void testAcr1255U() throws CardException {
        assertCommands(Acr1255UCardTerminal::new, "ACR1255U-J1 SWV 1.00",
                Arrays.asList("E00000200103", "E00000230101"),
                Arrays.asList("E00000230100"),
                Arrays.asList("E000001800"));
    }

    @Test
    public void testAcr1281() throws CardException {
        // enforce ISO14443-4, otherwise a DESFire is only activated at layer 3
        assertCommands(Acr1281CardTerminal::new, "ACR1281U_V521",
                Arrays.asList("E00000200103", "E00000230181"),
                Arrays.asList("E00000230100"),
                Arrays.asList("E000001800"));
    }

    @Test
    public void testAcr1283() throws CardException {
        assertCommands(Acr1283CardTerminal::new, "ACR1283L_V101",
                Arrays.asList("E00000200103", "E00000230101"),
                Arrays.asList("E00000230100"),
                Arrays.asList("E000001800"));
    }

    @Test
    public void testAcr1552() throws CardException {
        // ISO14443 type A and B are in the first byte of the polling type (03 00); 00 03 would poll for PicoPass only
        assertCommands(Acr1552CardTerminal::new, "ACR1552 R FW 1.01.01",
                Arrays.asList("E0000120020300", "E00000230101"),
                Arrays.asList("E00000230100"),
                Arrays.asList("E000001800"));
    }

    @Test
    public void testEscapeCommandValueNotConfirmed() throws CardException {
        // reader responds with success, but another value
        FakeCardTerminal terminal = new FakeCardTerminal("ACS ACR1281 1S Dual Reader PICC", () -> new FakeCard(command -> new byte[]{(byte) 0xE1, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00}));

        assertFalse(new Acr1281CardTerminal(terminal).setAutomaticPICCPolling(0x81));
        assertFalse(new Acr1552CardTerminal(terminal).setPICC(Acr1552CardTerminal.PICC_ISO14443_TYPE_A | Acr1552CardTerminal.PICC_ISO14443_TYPE_B));

        assertDisconnected(terminal);
    }

    @Test
    public void testEscapeCommandError() {
        // i.e. a reader which does not support the escape command
        FakeCardTerminal terminal = new FakeCardTerminal("ACS ACR1281 1S Dual Reader PICC", () -> new FakeCard(command -> new byte[]{0x63, 0x00}));

        Acr1281CardTerminal reader = new Acr1281CardTerminal(terminal);

        assertThrows(CardException.class, reader::startPolling);
        assertThrows(CardException.class, reader::getFirmware);

        assertDisconnected(terminal);
    }

    // responses captured from an ACR1552U (FW 1.01.01) with an NTAG216 and an original MIFARE Ultralight
    private static final String NTAG216_VERSION = "0004040201001303";
    private static final String NTAG216_SIGNATURE = "78575BA6DC2A4CB972513599E2B74FED2E40308F882C15EF22A8780851968E30";

    @Test
    public void testTransceiveTransparent() throws CardException {
        FakeCardTerminal terminal = new FakeCardTerminal("ACS ACR1552 1S CL Reader PICC", () -> new FakeCard(command -> {
            String hex = toHex(command);
            switch (hex) {
                case "FFC20000028100": // start session
                case "FFC20000028200": // end session
                    return fromHex("C0030090009000");
                case "FFC20002048F020003": // switch protocol
                    return fromHex("C0030090008F01009000");
                case "FFC2000103950160": // GET_VERSION
                    return fromHex("C00300900092010096020000" + "9708" + NTAG216_VERSION + "9000");
                case "FFC200010495023C00": // READ_SIG
                    return fromHex("C00300900092010096020000" + "9720" + NTAG216_SIGNATURE + "9000");
                case "FFC2000103950130": // card does not respond, i.e. not supported
                    return fromHex("C0030164019000");
                case "E00000230100": // automatic polling off
                    return fromHex("E10000000100");
                case "E00000230101": // automatic polling on
                    return fromHex("E10000000101");
                default:
                    return fromHex("6A81");
            }
        }));

        List<byte[]> responses = new Acr1552CardTerminal(terminal).transceiveTransparent(Arrays.asList(new byte[]{0x60}, new byte[]{0x3C, 0x00}, new byte[]{0x30}));

        assertEquals(NTAG216_VERSION, toHex(responses.get(0)));
        assertEquals(NTAG216_SIGNATURE, toHex(responses.get(1)));
        assertEquals(null, responses.get(2));

        // then the card is detected again by toggling automatic polling
        assertEquals(Arrays.asList("FFC20000028100", "FFC20002048F020003", "FFC2000103950160", "FFC200010495023C00", "FFC2000103950130", "FFC20000028200", "E00000230100", "E00000230101"), takeCommands(terminal));
    }

    @Test
    public void testTransceiveTransparentAcr1252() throws CardException {
        // the ACR1252 needs no restore after the session
        FakeCardTerminal terminal = new FakeCardTerminal("ACS ACR1252 1S CL Reader [ACR1252 Dual Reader PICC]", () -> new FakeCard(command -> {
            String hex = toHex(command);
            if(hex.equals("FFC20002048F020003")) {
                return fromHex("C0030090008F01009000");
            }
            if(hex.equals("FFC2000103950160")) {
                return fromHex("C00300900092010096020000" + "9708" + NTAG216_VERSION + "9000");
            }
            return fromHex("C0030090009000");
        }));

        List<byte[]> responses = new Acr1252CardTerminal(terminal).transceiveTransparent(Arrays.asList(new byte[]{0x60}));

        assertEquals(NTAG216_VERSION, toHex(responses.get(0)));
        assertEquals(Arrays.asList("FFC20000028100", "FFC20002048F020003", "FFC2000103950160", "FFC20000028200"), takeCommands(terminal));
    }

    @Test
    public void testTransceiveTransparentAcr122U() throws CardException {
        // NTAG: both commands answered, via PN532 InCommunicateThru
        FakeCardTerminal ntag = new FakeCardTerminal("ACS ACR122U 00 00", () -> new FakeCard(command -> {
            String hex = toHex(command);
            if(hex.equals("FF00000003D44260")) {
                return fromHex("D54300" + NTAG216_VERSION + "9000");
            }
            if(hex.equals("FF00000004D4423C00")) {
                return fromHex("D54300" + NTAG216_SIGNATURE + "9000");
            }
            return fromHex("6A81");
        }));

        List<byte[]> responses = new Acr122UCardTerminal(ntag).transceiveTransparent(Arrays.asList(new byte[]{0x60}, new byte[]{0x3C, 0x00}));

        assertEquals(NTAG216_VERSION, toHex(responses.get(0)));
        assertEquals(NTAG216_SIGNATURE, toHex(responses.get(1)));
        assertEquals(Arrays.asList("FF00000003D44260", "FF00000004D4423C00"), takeCommands(ntag));

        // original MIFARE Ultralight: no response (status 01), so the card is selected again after each command
        FakeCardTerminal ultralight = new FakeCardTerminal("ACS ACR122U 00 00", () -> new FakeCard(command -> {
            if(toHex(command).equals("FF00000004D44A0100")) {
                return fromHex("D54B010100440007048FE97AED2E809000");
            }
            return fromHex("D543019000");
        }));

        responses = new Acr122UCardTerminal(ultralight).transceiveTransparent(Arrays.asList(new byte[]{0x60}, new byte[]{0x3C, 0x00}));

        assertEquals(null, responses.get(0));
        assertEquals(null, responses.get(1));
        assertEquals(Arrays.asList("FF00000003D44260", "FF00000004D44A0100", "FF00000004D4423C00", "FF00000004D44A0100"), takeCommands(ultralight));
    }

    @Test
    public void testTransceiveTransparentSwitchProtocolFailure() throws CardException {
        FakeCardTerminal terminal = new FakeCardTerminal("ACS ACR1552 1S CL Reader PICC", () -> new FakeCard(command -> {
            if(toHex(command).equals("FFC20000028100") || toHex(command).equals("FFC20000028200")) {
                return fromHex("C0030090009000");
            }
            // switch protocol and exchange: no response from card
            return fromHex("C0030164019000");
        }));

        // a failed switch protocol is not fatal (the ACR1281U reports it for an NTAG216, but exchanges work)
        List<byte[]> responses = new Acr1552CardTerminal(terminal).transceiveTransparent(Arrays.asList(new byte[]{0x60}));
        assertEquals(null, responses.get(0));

        // session ended, then restore (automatic polling off fails here, so stops)
        assertEquals(Arrays.asList("FFC20000028100", "FFC20002048F020003", "FFC2000103950160", "FFC20000028200", "E00000230100"), takeCommands(terminal));
    }

    @Test
    public void testTransceiveTransparentNotSupported() throws CardException {
        FakeCardTerminal terminal = new FakeCardTerminal("ACS ACR1283 4S CL Reader PICC 00 00");

        assertEquals(null, new Acr1283CardTerminal(terminal).transceiveTransparent(Arrays.asList(new byte[]{0x60})));
        assertTrue(terminal.getConnected().isEmpty());
    }

    private static byte[] fromHex(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return bytes;
    }

    @Test
    public void testIdentifyTechnologies() {
        Acr1552CardTerminal reader = new Acr1552CardTerminal(new FakeCardTerminal("ACS ACR1552 1S CL Reader PICC"));

        // MIFARE Ultralight paper ticket
        assertEquals(Arrays.asList("MifareUltralight"), reader.identifyTechnologies(cardWithAtr("3B8F8001804F0CA0000003060300030000000068"), null));
        // DESFire
        assertEquals(Arrays.asList("IsoDep"), reader.identifyTechnologies(cardWithAtr("3B8180018080"), null));
        // MIFARE Classic 1K
        assertEquals(Collections.emptyList(), reader.identifyTechnologies(cardWithAtr("3B8F8001804F0CA000000306030001000000006A"), null));
    }

    private static FakeCard cardWithAtr(String hex) {
        byte[] atr = new byte[hex.length() / 2];
        for (int i = 0; i < atr.length; i++) {
            atr[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return new FakeCard() {
            @Override
            public ATR getATR() {
                return new ATR(atr);
            }
        };
    }

    private static void assertCommands(TerminalFactory factory, String firmware, List<String> start, List<String> stop, List<String> getFirmware) throws CardException {
        FakeCardTerminal terminal = new FakeCardTerminal("ACS reader", () -> new FakeCard(fakeAcsReader(firmware)));

        AcsExtendedCardTerminal reader = factory.create(terminal);

        reader.startPolling();
        assertEquals(start, takeCommands(terminal));

        reader.stopPolling();
        assertEquals(stop, takeCommands(terminal));

        assertEquals(firmware, reader.getFirmware());
        assertEquals(getFirmware, takeCommands(terminal));
    }

    /**
     * Fake ACS reader: confirms pseudo-APDU (FF 00 ..) and escape command (E0 00 ..) settings by echoing them,
     * and returns the firmware.
     */

    private static Function<byte[], byte[]> fakeAcsReader(String firmware) {
        byte[] firmwareBytes = firmware.getBytes(StandardCharsets.US_ASCII);
        return command -> {
            if(command[0] == (byte) 0xFF) {
                if(command[2] == 0x48) {
                    return firmwareBytes;
                }
                if(command[2] == 0x51) {
                    return new byte[]{(byte) 0x90, command[3]};
                }
            } else if(command[0] == (byte) 0xE0) {
                byte[] data;
                if(command[3] == 0x18) {
                    data = firmwareBytes;
                } else {
                    // echo the new value
                    data = Arrays.copyOfRange(command, 5, 5 + (command[4] & 0xFF));
                }
                byte[] response = new byte[5 + data.length];
                response[0] = (byte) 0xE1;
                response[4] = (byte) data.length;
                System.arraycopy(data, 0, response, 5, data.length);
                return response;
            }
            return new byte[]{0x63, 0x00};
        };
    }

    // commands since last call, each sent over a separate, disconnected, direct connection
    private static List<String> takeCommands(FakeCardTerminal terminal) {
        assertDisconnected(terminal);

        List<String> commands = new ArrayList<>();
        for (FakeCard card : terminal.getConnected()) {
            for (byte[] command : card.getControlCommands()) {
                commands.add(toHex(command));
            }
        }
        terminal.getConnected().clear();
        return commands;
    }

    private static void assertDisconnected(FakeCardTerminal terminal) {
        assertFalse(terminal.getConnected().isEmpty());
        for (FakeCard card : terminal.getConnected()) {
            assertTrue(card.isDisconnected());
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("%02X", b & 0xFF));
        }
        return builder.toString();
    }
}
