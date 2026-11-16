package no.entur.abt.nfc.example.acr;

import no.entur.abt.nfc.example.R;
import no.entur.android.nfc.external.acs.reader.Acr122UReader;

public class Acr122UControlsActivity extends AbstractAcrControlsActivity<Acr122UReader> {

	@Override
	protected Class<Acr122UReader> getReaderType() {
		return Acr122UReader.class;
	}

	@Override
	protected void addControls(Acr122UReader reader) {
		addSection(getString(R.string.acr_controls_buzzer));
		addRow(button(getString(R.string.acr_controls_card_detection_on), (v) -> run("setBuzzerForCardDetection(true)", () -> reader.setBuzzerForCardDetection(true))),
				button(getString(R.string.acr_controls_card_detection_off), (v) -> run("setBuzzerForCardDetection(false)", () -> reader.setBuzzerForCardDetection(false))));
	}
}
