package no.entur.android.nfc.websocket.server;

import org.junit.jupiter.api.Test;

import java.security.Provider;
import java.security.Security;
import java.util.List;

import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;
import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;

public class CardTerminals2Test {

    @Test
    public void test() throws Exception {

        String PROP_NAME = "sun.security.smartcardio.library";

        System.setProperty(PROP_NAME, "/usr/lib64/libpcsclite.so.1.0.0");

        //Object o = Class.forName("sun.security.smartcardio.SunPCSC").newInstance();

        //System.out.println(o);
        String type = "PC/SC";
        Provider sun = Security.getProvider("SunPCSC");
        if (sun == null) {
            @SuppressWarnings("deprecation")
            Object o = Class.forName("sun.security.smartcardio.SunPCSC").newInstance();
            sun = (Provider)o;
        }
        TerminalFactory factory = TerminalFactory.getInstance(type, null, sun);


        //CardTerminal cardReader = createCardReader(1000);

        System.out.println(factory);

    }

    @SuppressWarnings("restriction")
    public CardTerminal createCardReader(long timeout) throws CardException {
        TerminalFactory factory = TerminalFactory.getDefault();
    System.out.println(factory);

        CardTerminals cardTerminals = factory.terminals();

        List<CardTerminal> list = cardTerminals.list();
        System.out.println(cardTerminals);
        if(list.isEmpty()) {
            long deadline = System.currentTimeMillis() + timeout;

            while(true) {
                list = cardTerminals.list();
                if(!list.isEmpty()) {
                    break;
                }
                if(System.currentTimeMillis() > deadline) {
                    throw new CardException("No card terminals available after timeout of " + timeout + "ms");
                }
                Thread.yield();
            }
        }
        for(CardTerminal terminal : list) {
            //configureTerminal(terminal);
        }
        list = cardTerminals.list(CardTerminals.State.CARD_PRESENT);

        if(list.isEmpty()) {
            cardTerminals.waitForChange(timeout);
            list = cardTerminals.list(CardTerminals.State.CARD_PRESENT);
        }

        if (list.isEmpty()) {
            throw new CardException("No card terminals available");
        }
        return list.get(0);
    }
}
