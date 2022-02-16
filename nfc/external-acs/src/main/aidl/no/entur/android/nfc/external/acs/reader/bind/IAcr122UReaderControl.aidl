package no.entur.android.nfc.external.acs.reader.bind;

interface IAcr122UReaderControl {

	byte[] getFirmware();
	
	byte[] getPICC();

	byte[] setPICC(int picc);
	
	byte[] setBuzzerForCardDetection(boolean enable);

	byte[] control(int slotNum, int controlCode, in byte[] command);
		
	byte[] transmit(int slotNum, in byte[] command);

}
