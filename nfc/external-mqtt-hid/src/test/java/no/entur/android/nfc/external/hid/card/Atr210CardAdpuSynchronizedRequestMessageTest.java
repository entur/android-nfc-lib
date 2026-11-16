package no.entur.android.nfc.external.hid.card;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Collections;

import no.entur.android.nfc.external.hid.dto.atr210.ApduResponse;
import no.entur.android.nfc.external.hid.dto.atr210.NfcAdpuTransmitRequest;
import no.entur.android.nfc.external.hid.dto.atr210.NfcAdpuTransmitResponse;
import no.entur.android.nfc.mqtt.messages.card.CardAdpuSynchronizedRequestMessageRequest;

public class Atr210CardAdpuSynchronizedRequestMessageTest {

    private static final Atr210CardMessageConverter CONVERTER = new Atr210CardMessageConverter();

    @Test
    public void testUniqueCommandIds() {
        Atr210CardAdpuSynchronizedRequestMessage first = request();
        Atr210CardAdpuSynchronizedRequestMessage second = request();

        assertNotEquals(0, first.getCommandId());
        assertNotEquals(0, second.getCommandId());
        assertNotEquals(first.getCommandId(), second.getCommandId());
    }

    @Test
    public void testResponseMatchedByCommandId() {
        Atr210CardAdpuSynchronizedRequestMessage request = request();
        int commandId = request.getCommandId();

        assertTrue(request.isResponse(response(commandId)));
        // a late response to another request
        assertFalse(request.isResponse(response(commandId + 1)));
    }

    @Test
    public void testResponseWithoutCommandIdIsAccepted() {
        // i.e. the reader does not echo the command id
        assertTrue(request().isResponse(response(0)));
    }

    private static Atr210CardAdpuSynchronizedRequestMessage request() {
        Atr210CardContext context = new Atr210CardContext();
        context.setProviderId("itxpt.ticketreader.ATR210EH.1234567890");
        CardAdpuSynchronizedRequestMessageRequest<String, NfcAdpuTransmitRequest> request = CONVERTER.createCardAdpuRequestMessage(new byte[] { 0x60 }, context);
        return (Atr210CardAdpuSynchronizedRequestMessage) request;
    }

    private static Atr210CardAdpuSynchronizedResponseMessage response(int commandId) {
        ApduResponse apduResponse = new ApduResponse();
        apduResponse.setCommandId(commandId);
        apduResponse.setResponse("9100");

        NfcAdpuTransmitResponse response = new NfcAdpuTransmitResponse();
        response.setResult(Collections.singletonList(apduResponse));

        return new Atr210CardAdpuSynchronizedResponseMessage("itxpt.ticketreader.ATR210EH.1234567890/nfc/hf/apdu/response", response);
    }
}
