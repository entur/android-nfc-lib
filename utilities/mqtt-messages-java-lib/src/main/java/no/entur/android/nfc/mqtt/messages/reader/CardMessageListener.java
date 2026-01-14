package no.entur.android.nfc.mqtt.messages.reader;

import no.entur.android.nfc.mqtt.messages.card.CardContext;

public interface CardMessageListener<D, CC extends CardContext>  {

    void onCardLost(CardLeftFieldMessage card);

    void onCardPresent(CardEnteredFieldMessage<D, CC> card);
}
