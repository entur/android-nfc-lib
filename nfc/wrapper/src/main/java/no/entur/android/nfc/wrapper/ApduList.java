package no.entur.android.nfc.wrapper;

import android.os.Parcelable;

import java.util.List;

interface ApduList extends Parcelable {
	void add(byte[] command);

	List<byte[]> get();
}
