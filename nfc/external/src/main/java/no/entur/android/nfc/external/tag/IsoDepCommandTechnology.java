package no.entur.android.nfc.external.tag;

import android.nfc.tech.IsoDep;
import android.os.RemoteException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.entur.android.nfc.external.service.tag.CommandTechnology;
import no.entur.android.nfc.wrapper.TransceiveResult;
import no.entur.android.nfc.wrapper.tech.TagTechnology;

public class IsoDepCommandTechnology extends AbstractTagTechnology implements CommandTechnology {

	private static final Logger LOGGER = LoggerFactory.getLogger(IsoDepCommandTechnology.class);

	private DESFireAdapter adapter;
	private boolean hostCardEmulation;
	private TransceiveResultExceptionMapper mapper;

	public IsoDepCommandTechnology(AbstractReaderIsoDepWrapper isoDep, boolean hostCardEmulation, TransceiveResultExceptionMapper mapper) {
		super(TagTechnology.ISO_DEP);
		this.adapter = new DESFireAdapter(isoDep, false);
		this.hostCardEmulation = hostCardEmulation;
		this.mapper = mapper;
	}

	public TransceiveResult transceive(byte[] data, boolean raw) throws RemoteException {

		try {
			byte[] transceive;
			if (hostCardEmulation && data[0] == 0x00) {
				transceive = adapter.transceive(data);
			} else if (raw) {
				// we use desfire ev1 native command set
				// so do not wrap in an apdu here

				transceive = adapter.transceive(data);
			} else {
				transceive = adapter.transceive(data);
			}

			return new TransceiveResult(TransceiveResult.RESULT_SUCCESS, transceive);
		} catch (Exception e) {
			LOGGER.debug("Problem sending command", e);

			return mapper.mapException(e);
		}

	}

	public String toString() {
		return IsoDep.class.getSimpleName();
	}
}
