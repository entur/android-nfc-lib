package no.entur.android.nfc.external.acs.service;

import android.hardware.usb.UsbDevice;
import android.util.Log;

import com.acs.smartcard.ReaderException;

import no.entur.android.nfc.external.acs.reader.Acr1222LReader;
import no.entur.android.nfc.external.acs.reader.Acr122UReader;
import no.entur.android.nfc.external.acs.reader.Acr1251UReader;
import no.entur.android.nfc.external.acs.reader.Acr1252UReader;
import no.entur.android.nfc.external.acs.reader.Acr1255UReader;
import no.entur.android.nfc.external.acs.reader.Acr1281UReader;
import no.entur.android.nfc.external.acs.reader.Acr1283LReader;
import no.entur.android.nfc.external.acs.reader.AcrReader;
import no.entur.android.nfc.external.acs.reader.ReaderWrapper;
import no.entur.android.nfc.external.acs.reader.bind.IAcr1222LBinder;
import no.entur.android.nfc.external.acs.reader.bind.IAcr122UBinder;
import no.entur.android.nfc.external.acs.reader.bind.IAcr1251UBinder;
import no.entur.android.nfc.external.acs.reader.bind.IAcr1252UBinder;
import no.entur.android.nfc.external.acs.reader.bind.IAcr1255UBinder;
import no.entur.android.nfc.external.acs.reader.bind.IAcr1281UBinder;
import no.entur.android.nfc.external.acs.reader.bind.IAcr1283Binder;
import no.entur.android.nfc.external.acs.reader.command.ACR1222Commands;
import no.entur.android.nfc.external.acs.reader.command.ACR122Commands;
import no.entur.android.nfc.external.acs.reader.command.ACR1251Commands;
import no.entur.android.nfc.external.acs.reader.command.ACR1252Commands;
import no.entur.android.nfc.external.acs.reader.command.ACR1255UsbCommands;
import no.entur.android.nfc.external.acs.reader.command.ACR1281Commands;
import no.entur.android.nfc.external.acs.reader.command.ACR1283Commands;
import no.entur.android.nfc.external.acs.reader.command.ACRCommands;
import no.entur.android.nfc.external.acs.reader.command.ACRReaderTechnology;
import no.entur.android.nfc.external.service.ExternalUsbNfcServiceSupport;
import no.entur.android.nfc.external.service.tag.INFcTagBinder;

public class AcrReaderAdapter implements ExternalUsbNfcServiceSupport.ReaderAdapter<AcrReader> {

	private static final String TAG = AcrReaderAdapter.class.getName();
	private final ReaderWrapper reader;

	private IAcr122UBinder acr122Binder;
	private IAcr1222LBinder acr1222Binder;
	private IAcr1251UBinder acr1251Binder;
	private IAcr1281UBinder acr1281Binder;
	private IAcr1283Binder acr1283Binder;
	private IAcr1252UBinder acr1252Binder;
	private IAcr1255UBinder acr1255Binder;

	protected INFcTagBinder binder;

	public AcrReaderAdapter(ReaderWrapper reader, INFcTagBinder binder) {
		this.reader = reader;
		this.binder = binder;

		this.acr122Binder = new IAcr122UBinder();
		this.acr1222Binder = new IAcr1222LBinder();
		this.acr1251Binder = new IAcr1251UBinder();
		this.acr1281Binder = new IAcr1281UBinder();
		this.acr1283Binder = new IAcr1283Binder();
		this.acr1252Binder = new IAcr1252UBinder();
		this.acr1255Binder = new IAcr1255UBinder();
	}

	public ACRCommands getReaderCommands() {
		String name = reader.getReaderName();
		if (name != null) {
			if (name.contains("1222L")) {
				return new ACR1222Commands(name, reader);
			} else if (name.contains("122U")) {
				return new ACR122Commands(name, reader);
			} else if (name.contains("1251")) {
				return new ACR1251Commands(name, reader);
			} else if (name.contains("1281")) {
				return new ACR1281Commands(name, reader);
			} else if (name.contains("1283")) {
				return new ACR1283Commands(name, reader);
			} else if (name.contains("1252")) {
				return new ACR1252Commands(name, reader);
			} else if (name.contains("1255")) {
				return new ACR1255UsbCommands(name, reader);
			} else {
				Log.d(TAG, "No reader control for " + name);
			}
		}
		return new ACRCommands(reader);
	}

	@Override
	public void closeReader(UsbDevice device) {
		acr122Binder.clearReader();
		acr1222Binder.clearReader();
		acr1251Binder.clearReader();
		acr1252Binder.clearReader();
		acr1255Binder.clearReader();
		acr1281Binder.clearReader();
		acr1283Binder.clearReader();

		binder.setReaderTechnology(null);
	}

	@Override
	public AcrReader openReader(UsbDevice device) {
		reader.open(device);

		ACRCommands reader = getReaderCommands();

		try {
			binder.setReaderTechnology(new ACRReaderTechnology(reader));
		} catch (ReaderException e) {
			Log.d(TAG, "Problem initializing reader", e);
			return null;
		}

		return createUsbAcrReader(reader);
	}

	protected AcrReader createUsbAcrReader(ACRCommands reader) {
		if (reader instanceof ACR122Commands) {
			acr122Binder.setAcr122UCommands((ACR122Commands) reader);
			return new Acr122UReader(reader.getName(), acr122Binder);
		} else if (reader instanceof ACR1222Commands) {
			acr1222Binder.setAcr1222LCommands((ACR1222Commands) reader);
			return new Acr1222LReader(reader.getName(), acr1222Binder);
		} else if (reader instanceof ACR1251Commands) {
			acr1251Binder.setCommands((ACR1251Commands) reader);
			return new Acr1251UReader(reader.getName(), acr1251Binder);
		} else if (reader instanceof ACR1252Commands) {
			acr1252Binder.setCommands((ACR1252Commands) reader);
			return new Acr1252UReader(reader.getName(), acr1252Binder);
		} else if (reader instanceof ACR1255UsbCommands) {
			acr1255Binder.setCommands((ACR1255UsbCommands) reader);
			return new Acr1255UReader(reader.getName(), acr1255Binder);
		} else if (reader instanceof ACR1281Commands) {
			acr1281Binder.setCommands((ACR1281Commands) reader);
			return new Acr1281UReader(reader.getName(), acr1281Binder);
		} else if (reader instanceof ACR1283Commands) {
			acr1283Binder.setCommands((ACR1283Commands) reader);
			return new Acr1283LReader(reader.getName(), acr1283Binder);
		} else {
			Log.d(TAG, "Not supporting reader extras for " + reader.getName());
		}
		return null;
	}

	@Override
	public boolean isReaderSupported(UsbDevice device) {
		return reader.isSupported(device);
	}
}
