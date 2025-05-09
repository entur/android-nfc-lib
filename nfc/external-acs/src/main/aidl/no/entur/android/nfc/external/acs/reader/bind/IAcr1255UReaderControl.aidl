package no.entur.android.nfc.external.acs.reader.bind;

interface IAcr1255UReaderControl {

	byte[] getFirmware();

	byte[] getSerialNumber();

	byte[] getPICC();

	byte[] setPICC(int picc);

	byte[] getAutomaticPICCPolling();

	byte[] setAutomaticPICCPolling(int picc);

	byte[] setLEDs(int leds);

	byte[] getLEDs();

	byte[] getDefaultLEDAndBuzzerBehaviour();

	byte[] setDefaultLEDAndBuzzerBehaviour(int value);

	byte[] control(int slotNum, int controlCode, in byte[] command);
		
	byte[] transmit(int slotNum, in byte[] command);

	byte[] getAutoPPS();

	byte[] setAutoPPS(in byte[] speed);

	byte[] getAntennaFieldStatus();

	byte[] setAntennaField(boolean on);

	byte[] getBluetoothTransmissionPower();

	byte[] setBluetoothTransmissionPower(byte distance);

	byte[] setSleepModeOption(byte option);

	byte[] setAutomaticPolling(boolean on);

	byte[] getBatteryLevel();

	byte[] power(int slotNum, int action);

	byte[] setProtocol(int slotNum, int preferredProtocols);

    byte[] getState(int slotNum);

    byte[] getNumSlots();

}
