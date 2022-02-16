package no.entur.android.nfc.wrapper;

import java.io.IOException;

/**
 * 
 * Exception for handling situation seen in the wild: timeout with reconnect in underlying NFC stack results in an empty response array for isoDep targets.
 * 
 */

public class EmptyTransceiveResponseException extends IOException {
}
