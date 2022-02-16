package no.entur.android.nfc.external.acs.tag;

import android.nfc.tech.IsoDep;
import android.os.RemoteException;
import android.util.Log;

import java.io.IOException;

import com.acs.smartcard.ReaderException;

import no.entur.android.nfc.external.acs.reader.command.ACSIsoDepWrapper;
import no.entur.android.nfc.external.service.tag.CommandTechnology;
import no.entur.android.nfc.wrapper.TransceiveResult;
import no.entur.android.nfc.wrapper.tech.TagTechnology;

public class IsoDepAdapter extends DefaultTechnology implements CommandTechnology {

	protected static final String TAG = IsoDepAdapter.class.getName();

	private DESFireAdapter adapter;
	private boolean hostCardEmulation;

	public IsoDepAdapter(int slotNumber, ACSIsoDepWrapper isoDep, boolean hostCardEmulation) {
		super(TagTechnology.ISO_DEP, slotNumber);
		this.adapter = new DESFireAdapter(isoDep, false);
		this.hostCardEmulation = hostCardEmulation;
	}

	public TransceiveResult transceive(byte[] data, boolean raw) throws RemoteException {

		try {
			byte[] transceive;
			if (hostCardEmulation && data[0] == 0x00) {
				transceive = adapter.transceive(data);
			} else if (raw) {
				// we use desfire ev1 native command set
				// so do not wrap in an adpu here

				transceive = adapter.transceive(data);
			} else {
				transceive = adapter.transceive(data);
			}

			return new TransceiveResult(TransceiveResult.RESULT_SUCCESS, transceive);
		} catch (ReaderException | IOException e) {
			Log.d(TAG, "Problem sending command", e);

			return new TransceiveResult(TransceiveResult.RESULT_FAILURE, null);
		}

	}

	public String toString() {
		return IsoDep.class.getSimpleName();
	}
}
