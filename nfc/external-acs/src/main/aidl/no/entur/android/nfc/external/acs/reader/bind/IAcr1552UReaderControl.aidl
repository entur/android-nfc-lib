package no.entur.android.nfc.external.acs.reader.bind;

interface IAcr1552UReaderControl {

	byte[] getFirmware();
	
	byte[] getPICC();

	byte[] setPICC(int picc); // as in polling types

	byte[] getAutomaticPICCPolling();

	byte[] setAutomaticPICCPolling(int value);

	byte[] getAutomaticCommunicationSpeed();

	byte[] setAutomaticCommunicationSpeed(int speed);

	byte[] getRadioFrequencyPower();

	byte[] setRadioFrequencyPower(int power);

	byte[] control(int slotNum, int controlCode, in byte[] command);
		
	byte[] transmit(int slotNum, in byte[] command);

	byte[] setLEDs(int leds);

	byte[] getLEDs();

	byte[] getDefaultLEDAndBuzzerBehaviour(); // AKA UI Behaviour

	byte[] setDefaultLEDAndBuzzerBehaviour(int value); // AKA UI Behaviour

	byte[] setBuzzerControlSingle(int duration);

	byte[] setBuzzerControlRepeat(int onDuration, int offDuration, int repeats);

	byte[] power(int slotNum, int action);

	byte[] setProtocol(int slotNum, int preferredProtocols);

	byte[] getState(int slotNum);

	byte[] getNumSlots();

}
