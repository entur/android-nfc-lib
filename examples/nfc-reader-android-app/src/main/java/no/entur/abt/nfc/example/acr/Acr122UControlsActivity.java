package no.entur.abt.nfc.example.acr;

import java.util.Arrays;

import no.entur.abt.nfc.example.R;
import no.entur.android.nfc.external.acs.reader.Acr122UReader;
import no.entur.android.nfc.external.acs.reader.AcrPICC;

public class Acr122UControlsActivity extends AbstractAcrControlsActivity<Acr122UReader> {

	@Override
	protected Class<Acr122UReader> getReaderType() {
		return Acr122UReader.class;
	}

	@Override
	protected void addControls(Acr122UReader reader) {
		addSection(getString(R.string.acr_controls_picc));
		addFlags(AcrPICC.class, Arrays.asList(AcrPICC.AUTO_PICC_POLLING, AcrPICC.AUTO_ATS_GENERATION, AcrPICC.POLLING_INTERVAL, AcrPICC.POLL_ISO14443_TYPE_A, AcrPICC.POLL_ISO14443_TYPE_B, AcrPICC.POLL_FELICA_212K, AcrPICC.POLL_FELICA_424K, AcrPICC.POLL_TOPAZ), null, reader::getPICC, reader::setPICC);
		addSection(getString(R.string.acr_controls_buzzer));
		addSwitch(getString(R.string.acr_controls_card_detection_beep), null, reader::setBuzzerForCardDetection);
	}
}
