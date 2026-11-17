package no.entur.android.nfc.websocket.server;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import javax.smartcardio.CardException;

import no.entur.android.nfc.websocket.server.readers.Acr1222LCardTerminal;
import no.entur.android.nfc.websocket.server.readers.Acr122UCardTerminal;
import no.entur.android.nfc.websocket.server.readers.Acr1251CardTerminal;
import no.entur.android.nfc.websocket.server.readers.Acr1252CardTerminal;
import no.entur.android.nfc.websocket.server.readers.Acr1255UCardTerminal;
import no.entur.android.nfc.websocket.server.readers.Acr1281CardTerminal;
import no.entur.android.nfc.websocket.server.readers.Acr1283CardTerminal;
import no.entur.android.nfc.websocket.server.readers.Acr1552CardTerminal;

public class ExtendedCardTerminalFactoryTest {

    private final ExtendedCardTerminalFactory factory = new ExtendedCardTerminalFactory(new DefaultCardTerminalMetadataEnricher());

    @Test
    public void testReaderModels() {
        assertModel("ACS ACR122U PICC Interface 00 00", Acr122UCardTerminal.class);
        assertModel("ACS ACR1222L 3S PICC Reader 00 00", Acr1222LCardTerminal.class);
        assertModel("ACS ACR1251 1S CL Reader PICC 00 00", Acr1251CardTerminal.class);
        assertModel("ACS ACR1252 Dual Reader PICC", Acr1252CardTerminal.class);
        assertModel("ACS ACR1255U-J1 PICC 00 00", Acr1255UCardTerminal.class);
        assertModel("ACS ACR1281 1S Dual Reader PICC 00 00", Acr1281CardTerminal.class);
        assertModel("ACS ACR1283 4S CL Reader PICC 00 00", Acr1283CardTerminal.class);
        assertModel("ACS ACR1552 1S CL Reader PICC 00 00", Acr1552CardTerminal.class);
    }

    @Test
    public void testAcr1222LSamSlots() {
        // pcsc-lite names the SAM slots like the PICC slot
        assertModel("ACS ACR1222 3S PICC Reader 00 00", Acr1222LCardTerminal.class);
        assertEquals(null, factory.createTerminal(new FakeCardTerminal("ACS ACR1222 3S PICC Reader 00 01")));
        assertEquals(null, factory.createTerminal(new FakeCardTerminal("ACS ACR1222 3S PICC Reader 00 03")));
        // second reader, PICC slot
        assertModel("ACS ACR1222 3S PICC Reader 01 00", Acr1222LCardTerminal.class);
    }

    @Test
    public void testUnsupportedReader() {
        FakeCardTerminal terminal = new FakeCardTerminal("Yubico YubiKey OTP+FIDO+CCID 00 00");

        assertThrows(RuntimeException.class, () -> factory.create(terminal));
    }

    @Test
    public void testCreateStopsPollingAndEnriches() throws CardException {
        FakeCardTerminal terminal = new FakeCardTerminal("ACS ACR1281 1S Dual Reader PICC 00 00", () -> new FakeCard(command -> new byte[]{(byte) 0xE1, 0x00, 0x00, 0x00, 0x01, 0x00}));

        ExtendedCardTerminal extendedCardTerminal = factory.create(terminal);

        assertTrue(extendedCardTerminal instanceof Acr1281CardTerminal);
        assertSame(terminal, extendedCardTerminal.getDelegate());
        assertEquals(Collections.emptyList(), extendedCardTerminal.getTags());

        // automatic polling off
        assertEquals(1, terminal.getConnected().size());
        FakeCard card = terminal.getConnected().get(0);
        assertArrayEquals(new byte[]{(byte) 0xE0, 0x00, 0x00, 0x23, 0x01, 0x00}, card.getControlCommands().get(0));
        assertTrue(card.isDisconnected());
    }

    @Test
    public void testAdditionalReader() throws CardException {
        ExtendedCardTerminalFactory custom = new ExtendedCardTerminalFactory(new DefaultCardTerminalMetadataEnricher()) {
            @Override
            protected ExtendedCardTerminal createTerminal(javax.smartcardio.CardTerminal cardTerminal) {
                if(cardTerminal.getName().contains("Custom")) {
                    return new Acr1251CardTerminal(cardTerminal);
                }
                return super.createTerminal(cardTerminal);
            }
        };

        FakeCardTerminal terminal = new FakeCardTerminal("Custom Reader", () -> new FakeCard(command -> new byte[]{(byte) 0x90, 0x00}));

        assertTrue(custom.create(terminal) instanceof Acr1251CardTerminal);
    }

    private void assertModel(String name, Class<?> type) {
        FakeCardTerminal terminal = new FakeCardTerminal(name);

        ExtendedCardTerminal extendedCardTerminal = factory.createTerminal(terminal);

        assertEquals(type, extendedCardTerminal.getClass(), name);
    }
}
