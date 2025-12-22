package no.entur.android.nfc.external.hid.test.configuration;

import no.entur.android.nfc.external.hid.dto.atr210.NfcConfiguationRequest;
import no.entur.android.nfc.external.hid.dto.atr210.NfcConfiguationResponse;
import no.entur.android.nfc.external.hid.dto.atr210.ReadersStatusResponse;

public interface Atr210ConfigurationListener {

    NfcConfiguationResponse onGetConfiguration();

    ReadersStatusResponse onGetReaders();

    NfcConfiguationResponse onSetConfiguration(NfcConfiguationRequest request);

}
