package no.entur.android.nfc.external.hid.test.configuration;

import no.entur.android.nfc.external.hid.dto.atr210.NfcConfiguationRequest;
import no.entur.android.nfc.external.hid.dto.atr210.NfcConfiguationResponse;
import no.entur.android.nfc.external.hid.dto.atr210.ReaderStatus;
import no.entur.android.nfc.external.hid.dto.atr210.ReadersStatusResponse;

public class DefaultAtr210ConfigurationListener implements Atr210ConfigurationListener {

    private ReaderStatus samReader;
    private ReaderStatus hfReader;

    private boolean enabledSamReader;
    private boolean enabledHfReader;

    private boolean enabled;

    @Override
    public NfcConfiguationResponse onGetConfiguration() {

        NfcConfiguationResponse configuration = new NfcConfiguationResponse();

        if(enabledSamReader && samReader != null) {
            configuration.setSamId(samReader.getId());
            configuration.setSamName(samReader.getName());
        } else {
            configuration.setSamId("none");
        }

        if(enabledHfReader && hfReader != null) {
            configuration.setHfId(hfReader.getId());
            configuration.setHfName(hfReader.getName());
        } else {
            configuration.setHfId("none");
        }

        configuration.setEnabled(enabled);

        return configuration;
    }

    public void setSamReader(String name, String id) {
        ReaderStatus samReader = new ReaderStatus();

        samReader.setName(name);
        samReader.setId(id);

        this.samReader = samReader;
    }

    public void setHfReader(String name, String id) {
        ReaderStatus hfReader = new ReaderStatus();

        hfReader.setName(name);
        hfReader.setId(id);

        this.hfReader = hfReader;
    }

    public void clearHfReader() {
        this.hfReader = null;
        this.enabledHfReader = false;
    }

    public void clearSamReader() {
        this.samReader = null;
        this.enabledSamReader = false;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public ReadersStatusResponse onGetReaders() {
        ReadersStatusResponse readersStatus = new ReadersStatusResponse();

        if(hfReader != null) {
            readersStatus.addHfReader(hfReader);
        }

        if(samReader != null) {
            readersStatus.addSamReader(samReader);
        }

        return readersStatus;
    }

    @Override
    public NfcConfiguationResponse onSetConfiguration(NfcConfiguationRequest request) {

        if(!validateHfReader(request)) {
            throw new RuntimeException();
        }
        if(!validateSamReader(request)) {
            throw new RuntimeException();
        }

        this.enabledHfReader = !(request.getHfId() == null || request.getHfId().equals("none"));
        this.enabledSamReader = !(request.getSamId() == null || request.getSamId().equals("none"));

        this.enabled = request.getEnabled() != null && request.getEnabled();

        return onGetConfiguration();
    }

    private boolean validateSamReader(NfcConfiguationRequest request) {
        if(request.getSamId() == null || request.getSamId().equals("none")) {
            return true;
        }

        if(samReader == null) {
            return false;
        }

        return request.getSamId().equals(samReader.getId());
    }

    private boolean validateHfReader(NfcConfiguationRequest request) {
        if(request.getHfId() == null || request.getHfId().equals("none")) {
            return true;
        }

        if(hfReader == null) {
            return false;
        }

        return request.getHfId().equals(hfReader.getId());
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isEnabledHfReader() {
        return enabledHfReader;
    }

    public boolean isEnabledSamReader() {
        return enabledSamReader;
    }

    public void setEnabledHfReader(boolean enabledHfReader) {
        this.enabledHfReader = enabledHfReader;
    }

    public void setEnabledSamReader(boolean enabledSamReader) {
        this.enabledSamReader = enabledSamReader;
    }
}
