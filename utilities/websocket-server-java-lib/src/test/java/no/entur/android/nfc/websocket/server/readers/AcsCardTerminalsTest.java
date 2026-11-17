package no.entur.android.nfc.websocket.server.readers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

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
        assertCommands(Acr1281CardTerminal::new, "ACR1281U_V521",
                Arrays.asList("E00000200103", "E00000230101"),
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
