package no.entur.android.nfc.websocket.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;

public class FakeCard extends Card {

    private final List<byte[]> controlCommands = Collections.synchronizedList(new ArrayList<>());

    private final Function<byte[], byte[]> controlResponses;

    private volatile boolean disconnected = false;

    public FakeCard() {
        this((c) -> new byte[] {(byte) 0x90, 0x00});
    }

    public FakeCard(Function<byte[], byte[]> controlResponses) {
        this.controlResponses = controlResponses;
    }

    @Override
    public ATR getATR() {
        return new ATR(new byte[]{0x3B, (byte) 0x81, (byte) 0x80, 0x01, (byte) 0x80, (byte) 0x80});
    }

    @Override
    public String getProtocol() {
        return "T=1";
    }

    @Override
    public CardChannel getBasicChannel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CardChannel openLogicalChannel() throws CardException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void beginExclusive() throws CardException {
        checkConnected();
    }

    @Override
    public void endExclusive() throws CardException {
        checkConnected();
    }

    @Override
    public byte[] transmitControlCommand(int controlCode, byte[] command) throws CardException {
        checkConnected();
        controlCommands.add(command);
        return controlResponses.apply(command);
    }

    @Override
    public void disconnect(boolean reset) throws CardException {
        checkConnected();
        disconnected = true;
    }

    private void checkConnected() {
        if(disconnected) {
            throw new IllegalStateException("Card has been disconnected");
        }
    }

    public boolean isDisconnected() {
        return disconnected;
    }

    public List<byte[]> getControlCommands() {
        return controlCommands;
    }
}
