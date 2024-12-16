package no.entur.android.nfc.external.acs.reader;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

import com.acs.smartcard.ReaderException;

import java.util.ArrayList;
import java.util.List;

import no.entur.android.nfc.external.acs.reader.bind.IAcr1552UReaderControl;
import no.entur.android.nfc.external.acs.reader.command.remote.IAcr1552UCommandWrapper;

public class Acr1552UReader extends AcrReader {

	private static final String TAG = Acr1552UReader.class.getName();

	// bit 3 and 7 RFU
	private static final int POLL_SRI_OR_SRIX = 1 << 6;
	private static final int POLL_INNOVATRON = 1 << 5;
	private static final int POLL_TOPAZ = 1 << 4;
	private static final int POLL_FELICA_212K = 1 << 2;
	private static final int POLL_ISO14443_TYPE_B = 1 << 1;
	private static final int POLL_ISO14443_TYPE_A = 1 << 0;

	// bit 4-7 RFU
	private static final int POLL_CTS = 1 << 3;
	private static final int POLL_ISO15693 = 1 << 2;
	private static final int POLL_PICOPASS_ISO15693 = 1 << 1;
	private static final int POLL_PICOPASS_ISO14443B = 1;

	private static final int RATE_106K = 0;
	private static final int RATE_212K = 1;
	private static final int RATE_424K = 2;
	private static final int RATE_848K = 3;

	private static final int POWER_AUTO = 0;
	private static final int POWER_20_PERCENT = 0x01;
	private static final int POWER_40_PERCENT = 0x02;
	private static final int POWER_60_PERCENT = 0x03;
	private static final int POWER_80_PERCENT = 0x04;
	private static final int POWER_100_PERCENT = 0x05;

	public static final int LED_GREEN = 1 << 1;
	public static final int LED_BLUE = 1;

	private static final int UI_ACCESSING_LED = 1;
	private static final int UI_PICC_POLLING_STATUS_LED = 1 << 1;
	private static final int UI_PICC_ACTIVATION_STATUS_LED = 1 << 2;
	private static final int UI_PRESENCE_EVENT_BUZZER = 1 << 3;
	private static final int UI_REMOVAL_EVENT_BUZZER = 1 << 4;

	protected IAcr1552UReaderControl readerControl;

	public Acr1552UReader(String name, IAcr1552UReaderControl readerControl) {
		this.readerControl = readerControl;
		this.name = name;
	}

	public static int serializeBehaviour(AcrDefaultLEDAndBuzzerBehaviour... types) {
		int operation = 0;

		for (AcrDefaultLEDAndBuzzerBehaviour type : types) {
			if (type == AcrDefaultLEDAndBuzzerBehaviour.CARD_OPERATION_BLINK_LED) {
				operation |= UI_ACCESSING_LED;
			} else if (type == AcrDefaultLEDAndBuzzerBehaviour.PICC_POLLING_STATUS_LED) {
				operation |= UI_PICC_POLLING_STATUS_LED;
			} else if (type == AcrDefaultLEDAndBuzzerBehaviour.PICC_ACTIVATION_STATUS_LED) {
				operation |= UI_PICC_ACTIVATION_STATUS_LED;
			} else if (type == AcrDefaultLEDAndBuzzerBehaviour.CARD_INSERTION_EVENT_BUZZER) {
				operation |= UI_PRESENCE_EVENT_BUZZER;
			} else if (type == AcrDefaultLEDAndBuzzerBehaviour.CARD_REMOVAL_EVENT_BUZZER) {
				operation |= UI_REMOVAL_EVENT_BUZZER;
			} else {
				throw new IllegalArgumentException("Behaviour " + type + " not supported");
			}
		}
		return operation;
	}

	public static List<AcrDefaultLEDAndBuzzerBehaviour> parseBehaviour(int operation) {
		List<AcrDefaultLEDAndBuzzerBehaviour> behaviours = new ArrayList<AcrDefaultLEDAndBuzzerBehaviour>();

		if ((operation & UI_ACCESSING_LED) != 0) {
			behaviours.add(AcrDefaultLEDAndBuzzerBehaviour.CARD_OPERATION_BLINK_LED);
		}

		if ((operation & UI_PICC_POLLING_STATUS_LED) != 0) {
			behaviours.add(AcrDefaultLEDAndBuzzerBehaviour.PICC_POLLING_STATUS_LED);
		}

		if ((operation & UI_PICC_ACTIVATION_STATUS_LED) != 0) {
			behaviours.add(AcrDefaultLEDAndBuzzerBehaviour.PICC_ACTIVATION_STATUS_LED);
		}

		if ((operation & UI_PRESENCE_EVENT_BUZZER) != 0) {
			behaviours.add(AcrDefaultLEDAndBuzzerBehaviour.CARD_INSERTION_EVENT_BUZZER);
		}

		if ((operation & UI_REMOVAL_EVENT_BUZZER) != 0) {
			behaviours.add(AcrDefaultLEDAndBuzzerBehaviour.CARD_REMOVAL_EVENT_BUZZER);
		}

		return behaviours;
	}

	public String getFirmware() throws AcrReaderException {

		byte[] response;
		try {
			response = readerControl.getFirmware();
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}

		return readString(response);
	}

	public List<AcrPICC> getPICC() {
		byte[] response;
		try {
			response = readerControl.getPICC();
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}

		int operation = readInteger(response);

		int byte0 = operation & 0xFF;
		int byte1 = (operation >> 8) & 0xFF;

		ArrayList<AcrPICC> values = new ArrayList<AcrPICC>();

		if ((byte1 & POLL_SRI_OR_SRIX) != 0) {
			values.add(AcrPICC.POLL_SRI_OR_SRIX);
		}
		if ((byte1 & POLL_INNOVATRON) != 0) {
			values.add(AcrPICC.POLL_INNOVATRON);
		}
		if ((byte1 & POLL_TOPAZ) != 0) {
			values.add(AcrPICC.POLL_TOPAZ);
		}
		if ((byte1 & POLL_FELICA_212K) != 0) {
			values.add(AcrPICC.POLL_FELICA_212K);
		}
		if ((byte1 & POLL_ISO14443_TYPE_B) != 0) {
			values.add(AcrPICC.POLL_ISO14443_TYPE_B);
		}
		if ((byte1 & POLL_ISO14443_TYPE_A) != 0) {
			values.add(AcrPICC.POLL_ISO14443_TYPE_A);
		}

		if ((byte0 & POLL_CTS) != 0) {
			values.add(AcrPICC.POLL_CTS);
		}
		if ((byte0 & POLL_ISO15693) != 0) {
			values.add(AcrPICC.POLL_ISO15693);
		}
		if ((byte0 & POLL_PICOPASS_ISO15693) != 0) {
			values.add(AcrPICC.POLL_PICOPASS_ISO15693);
		}
		if ((byte0 & POLL_PICOPASS_ISO14443B) != 0) {
			values.add(AcrPICC.POLL_PICOPASS_ISO14443B);
		}

		return values;
	}

	public boolean setPICC(AcrPICC... types) {

		int byte0 = 0;
		int byte1 = 0;

		for (AcrPICC type : types) {
			switch (type) {

				// byte 1
				case POLL_SRI_OR_SRIX: {
					byte1 |= POLL_SRI_OR_SRIX;
					break;
				}
				case POLL_INNOVATRON: {
					byte1 |= POLL_INNOVATRON;
					break;
				}
				case POLL_TOPAZ: {
					byte1 |= POLL_TOPAZ;
					break;
				}
				case POLL_FELICA_212K: {
					byte1 |= POLL_FELICA_212K;
					break;
				}

				case POLL_ISO14443_TYPE_A: {
					byte1 |= POLL_ISO14443_TYPE_A;
					break;
				}
				case POLL_ISO14443_TYPE_B: {
					byte1 |= POLL_ISO14443_TYPE_B;
					break;
				}

				// byte 0
				case POLL_CTS: {
					byte0 |= POLL_CTS;
					break;
				}
				case POLL_ISO15693: {
					byte0 |= POLL_ISO15693;
					break;
				}
				case POLL_PICOPASS_ISO15693: {
					byte0 |= POLL_PICOPASS_ISO15693;
					break;
				}
				case POLL_PICOPASS_ISO14443B: {
					byte0 |= POLL_PICOPASS_ISO14443B;
					break;
				}

				default: {
					throw new IllegalArgumentException("Unexpected PICC " + type);
				}
			}
		}

		int picc = (byte1 << 8) | byte0;

		byte[] response;
		try {
			response = readerControl.setPICC(picc);
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}

		return readBoolean(response);
	}

	public List<AcrAutomaticPICCPolling> getAutomaticPICCPolling() {
		byte[] response;
		try {
			response = readerControl.getAutomaticPICCPolling();
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}

		return AcrAutomaticPICCPolling.parse1552(readInteger(response));
	}

	public boolean setAutomaticPICCPolling(AcrAutomaticPICCPolling... types) {
		byte[] response;
		try {
			response = readerControl.setAutomaticPICCPolling(AcrAutomaticPICCPolling.serialize(types));
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}

		return readBoolean(response);
	}

	public List<AcrDefaultLEDAndBuzzerBehaviour> getDefaultLEDAndBuzzerBehaviour() {
		byte[] response;
		try {
			response = readerControl.getDefaultLEDAndBuzzerBehaviour();
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}

		return parseBehaviour(readInteger(response));
	}

	public boolean setDefaultLEDAndBuzzerBehaviour(AcrDefaultLEDAndBuzzerBehaviour... types) {
		byte[] response;
		try {
			int operation = serializeBehaviour(types);

			response = readerControl.setDefaultLEDAndBuzzerBehaviour(operation);
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}

		return readBoolean(response);
	}

	public List<AcrLED> getLEDs() {
		byte[] response;
		try {
			response = readerControl.getLEDs();
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}

		List<AcrLED> leds = new ArrayList<AcrLED>();

		if ((response[0] & LED_BLUE) != 0) {
			leds.add(AcrLED.BLUE);
		}
		if ((response[0] & LED_GREEN) != 0) {
			leds.add(AcrLED.GREEN);
		}

		return leds;
	}

	public boolean setLEDs(AcrLED... types) {
		byte[] response;
		try {
			int operation = 0;
			for (AcrLED type : types) {
				if (type == AcrLED.GREEN) {
					operation |= LED_GREEN;
				} else if (type == AcrLED.BLUE) {
					operation |= LED_BLUE;
				} else {
					throw new IllegalArgumentException("LED " + type + " not supported");
				}
			}
			response = readerControl.setLEDs(operation);
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}

		return readBoolean(response);
	}

	/**
	 *
	 * Set RF power
	 *
	 * @param percent power in percent
	 * @return true if power was updated
	 * @throws ReaderException
	 */

	public boolean setRadioFrequencyPower(int percent) {
		byte[] response;
		try {
			response = readerControl.setRadioFrequencyPower(serializePower(percent));
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}

		return readBoolean(response);
	}

	/**
	 * Get RF power
	 *
	 * @return power in percent
	 */

	public int getRadioFrequencyPower() {
		byte[] response;
		try {
			response = readerControl.getRadioFrequencyPower();
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}
		int parameter = readInteger(response);

		return deserializePower(parameter);
	}

	public boolean setBuzzerControlSingle(int durationInMillis) {
		if (durationInMillis > 2550) {
			throw new IllegalArgumentException("Max 2550 millis");
		}
		byte[] response;
		try {
			int operation = durationInMillis / 10;

			response = readerControl.setBuzzerControlSingle(operation);
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}

		return readBoolean(response);
	}

	public boolean setBuzzerControlRepeat(int onDuration, int offDuration, int repeats) {
		if (onDuration > 2550) {
			throw new IllegalArgumentException("Max 2550 millis");
		}
		if (offDuration > 2550) {
			throw new IllegalArgumentException("Max 2550 millis");
		}
		if (repeats > 255) {
			throw new IllegalArgumentException("Max 255 repeats");
		}
		byte[] response;
		try {
			response = readerControl.setBuzzerControlRepeat(onDuration / 10, offDuration / 10, repeats);
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}

		return readBoolean(response);
	}

	public boolean setAutomaticCommunicationSpeed(AcrCommunicationSpeed kbps) {
		byte[] response;
		try {
			response = readerControl.setAutomaticCommunicationSpeed(serializeSpeed(kbps));
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}

		return readBoolean(response);
	}

	public AcrCommunicationSpeed getAutomaticCommunicationSpeed() {
		byte[] response;
		try {
			response = readerControl.getAutomaticCommunicationSpeed();
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}
		int[] parameter = readIntegers(response);

		return deserializeSpeed(parameter[1]);
	}

	private static AcrCommunicationSpeed deserializeSpeed(int kbps) {
		switch(kbps) {
			case RATE_106K:
				return AcrCommunicationSpeed.RATE_106_KBPS;
			case RATE_212K:
				return AcrCommunicationSpeed.RATE_212_KBPS;
			case RATE_424K:
				return AcrCommunicationSpeed.RATE_424_KBPS;
			case RATE_848K:
				return AcrCommunicationSpeed.RATE_848_KBPS;
			default: throw new IllegalArgumentException("Expected 106, 212, 424 or 848");
		}
	}

	private static int serializeSpeed(AcrCommunicationSpeed kbps) {
		switch(kbps) {
			case RATE_106_KBPS:
				return RATE_106K;
			case RATE_212_KBPS:
				return RATE_212K;
			case RATE_424_KBPS:
				return RATE_424K;
			case RATE_848_KBPS:
				return RATE_848K;
			default: throw new IllegalArgumentException("Expected 106, 212, 424 or 848");
		}
	}

	private static int deserializePower(int value) {
		switch(value) {
			case POWER_AUTO:
				return 0;
			case POWER_20_PERCENT:
				return 20;
			case POWER_40_PERCENT:
				return 40;
			case POWER_60_PERCENT:
				return 60;
			case POWER_80_PERCENT:
				return 80;
			case POWER_100_PERCENT:
				return 100;
			default: throw new IllegalArgumentException();
		}
	}

	private static int serializePower(int kbps) {
		switch(kbps) {
			case 0:
				return POWER_AUTO;
			case 20:
				return POWER_20_PERCENT;
			case 40:
				return POWER_40_PERCENT;
			case 60:
				return POWER_60_PERCENT;
			case 80:
				return POWER_80_PERCENT;
			case 100:
				return POWER_100_PERCENT;
			default: throw new IllegalArgumentException("Expected 0, 20, 40, 60, 80 or 100");
		}
	}


	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeStrongBinder(readerControl.asBinder());
	}

	public static final Creator<Acr1552UReader> CREATOR = new Creator<Acr1552UReader>() {
		@Override
		public Acr1552UReader createFromParcel(Parcel in) {
			String name = in.readString();

			IBinder binder = in.readStrongBinder();

			IAcr1552UReaderControl iin = IAcr1552UReaderControl.Stub.asInterface(binder);

			return new Acr1552UReader(name, iin);

		}

		@Override
		public Acr1552UReader[] newArray(int size) {
			return new Acr1552UReader[size];
		}
	};

	@Override
	public byte[] control(int slotNum, int controlCode, byte[] command) {
		byte[] response;
		try {
			response = readerControl.control(slotNum, controlCode, command);
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}

		return readByteArray(response);
	}

	@Override
	public byte[] transmit(int slotNum, byte[] command) {
		byte[] response;
		try {
			response = readerControl.transmit(slotNum, command);
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}

		return readByteArray(response);
	}


}