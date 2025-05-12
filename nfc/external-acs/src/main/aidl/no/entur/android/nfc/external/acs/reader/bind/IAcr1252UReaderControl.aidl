package no.entur.android.nfc.external.acs.reader.bind;

interface IAcr1252UReaderControl {

	byte[] getFirmware();
	
	byte[] getPICC();

	byte[] setPICC(int picc);
	
	byte[] control(int slotNum, int controlCode, in byte[] command);
		
	byte[] transmit(int slotNum, in byte[] command);

	byte[] getDefaultLEDAndBuzzerBehaviour();

	byte[] setDefaultLEDAndBuzzerBehaviour(int value);

	byte[] setLEDs(int leds);

	byte[] getLEDs();

	byte[] getAutomaticPICCPolling();

	byte[] setAutomaticPICCPolling(int picc);

	byte[] setBuzzer(boolean enable);

	byte[] power(int slotNum, int action);

	byte[] setProtocol(int slotNum, int preferredProtocols);

	byte[] getState(int slotNum);

	byte[] getNumSlots();

}
