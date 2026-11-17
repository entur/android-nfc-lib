package no.entur.android.nfc.websocket.android;

import no.entur.android.nfc.external.tag.TransceiveResultExceptionMapper;
import no.entur.android.nfc.websocket.messages.card.CardLostException;
import no.entur.android.nfc.wrapper.TransceiveResult;

/**
 * Maps a lost card to a lost tag (TagLostException), like with Android's own NFC reader.
 */

public class WebsocketTransceiveResultExceptionMapper implements TransceiveResultExceptionMapper {
    @Override
    public TransceiveResult mapException(Exception e) {
        if(e instanceof CardLostException) {
            return new TransceiveResult(TransceiveResult.RESULT_TAGLOST, null);
        }
        return new TransceiveResult(TransceiveResult.RESULT_FAILURE, null);
    }
}
