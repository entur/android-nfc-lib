package no.entur.android.nfc.websocket.server;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

public class FakeCardTerminal extends CardTerminal {

    private final String name;

    private volatile boolean cardPresent = false;

    private final Supplier<FakeCard> cards;

    private final List<FakeCard> connected = new CopyOnWriteArrayList<>();

    public FakeCardTerminal(String name) {
        this(name, FakeCard::new);
    }

    public FakeCardTerminal(String name, Supplier<FakeCard> cards) {
        this.name = name;
        this.cards = cards;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Card connect(String protocol) throws CardException {
        FakeCard card = cards.get();
        connected.add(card);
        return card;
    }

    @Override
    public boolean isCardPresent() throws CardException {
        return cardPresent;
    }

    public void setCardPresent(boolean cardPresent) {
        this.cardPresent = cardPresent;
    }

    @Override
    public boolean waitForCardPresent(long timeout) throws CardException {
        return cardPresent;
    }

    @Override
    public boolean waitForCardAbsent(long timeout) throws CardException {
        return !cardPresent;
    }

    public List<FakeCard> getConnected() {
        return connected;
    }
}
