package no.entur.abt.nfc.example.acr;

import java.util.Arrays;

import no.entur.abt.nfc.example.R;
import no.entur.android.nfc.external.acs.reader.Acr1251UReader;
import no.entur.android.nfc.external.acs.reader.AcrPICC;

public class Acr1251UControlsActivity extends AbstractAcrControlsActivity<Acr1251UReader> {

	@Override
	protected Class<Acr1251UReader> getReaderType() {
		return Acr1251UReader.class;
	}

	@Override
	protected void addControls(Acr1251UReader reader) {
		addSection(getString(R.string.acr_controls_picc));
		addFlags(AcrPICC.class, Arrays.asList(AcrPICC.POLL_ISO14443_TYPE_A, AcrPICC.POLL_ISO14443_TYPE_B, AcrPICC.POLL_FELICA_212K, AcrPICC.POLL_FELICA_424K, AcrPICC.POLL_TOPAZ), null, reader::getPICC, reader::setPICC);
	}
}
