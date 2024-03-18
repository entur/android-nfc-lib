package no.entur.android.nfc.external.tag;

import no.entur.android.nfc.wrapper.TransceiveResult;

/**
 *
 * Exception mapper for more detailed {@linkplain TransceiveResult}.
 *
 */
public interface TransceiveResultExceptionMapper {

    TransceiveResult mapException(Exception e);

}
