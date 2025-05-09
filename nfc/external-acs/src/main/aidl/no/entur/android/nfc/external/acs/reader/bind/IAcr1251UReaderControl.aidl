package no.entur.android.nfc.external.acs.reader.bind;

interface IAcr1251UReaderControl {

	byte[] getFirmware();
	
	byte[] getPICC();

	byte[] setPICC(int picc);
	
	byte[] control(int slotNum, int controlCode, in byte[] command);
		
	byte[] transmit(int slotNum, in byte[] command);

	byte[] power(int slotNum, int action);

	byte[] setProtocol(int slotNum, int preferredProtocols);

    byte[] getState(int slotNum);

    byte[] getNumSlots();

}
