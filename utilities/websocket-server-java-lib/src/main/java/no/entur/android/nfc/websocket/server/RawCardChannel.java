package no.entur.android.nfc.websocket.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;

/**
 * Transmit bytes to a card as-is.
 * <br>
 * The jnasmartcardio provider treats every command as an ISO 7816 APDU: it rewrites the class byte if bit 8 is not set
 * (i.e. a native DESFire command 5A becomes 18), and expects a status word in the response (a native DESFire response
 * can be a single byte). So for contactless (T=1) cards, use its raw transmit instead.
 */

public class RawCardChannel {

    private final static Logger LOGGER = LoggerFactory.getLogger(RawCardChannel.class);

    private static final int MAX_RESPONSE_LENGTH = 8192;

    private static final Class<?> JNA_CARD_CHANNEL;
    private static final Method JNA_TRANSMIT_RAW;

    static {
        Class<?> channelClass = null;
        Method transmitRaw = null;
        try {
            channelClass = Class.forName("jnasmartcardio.Smartcardio$JnaCardChannel");
            transmitRaw = channelClass.getDeclaredMethod("transmitRaw", ByteBuffer.class, ByteBuffer.class);
            transmitRaw.setAccessible(true);
        } catch (Exception e) {
            LOGGER.debug("Raw transmit not available", e);
            channelClass = null;
            transmitRaw = null;
        }
        JNA_CARD_CHANNEL = channelClass;
        JNA_TRANSMIT_RAW = transmitRaw;
    }

    public static boolean isRawTransmit(Card card, CardChannel channel) {
        return JNA_TRANSMIT_RAW != null && JNA_CARD_CHANNEL.isInstance(channel) && "T=1".equals(card.getProtocol());
    }

    public static byte[] transmit(Card card, CardChannel channel, byte[] command) throws CardException {
        if(isRawTransmit(card, channel)) {
            ByteBuffer response = ByteBuffer.allocate(MAX_RESPONSE_LENGTH);
            try {
                JNA_TRANSMIT_RAW.invoke(channel, ByteBuffer.wrap(command), response);
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if(cause instanceof CardException) {
                    throw (CardException) cause;
                }
                throw new CardException(cause);
            } catch (IllegalAccessException e) {
                throw new CardException(e);
            }
            byte[] result = new byte[response.position()];
            response.flip();
            response.get(result);
            return result;
        }

        ByteBuffer response = ByteBuffer.allocate(MAX_RESPONSE_LENGTH);
        int count = channel.transmit(ByteBuffer.wrap(command), response);
        byte[] result = new byte[count];
        System.arraycopy(response.array(), 0, result, 0, count);
        return result;
    }
}
