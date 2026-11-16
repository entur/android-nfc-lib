package no.entur.android.nfc.external.acs.reader;

public enum AcrDefaultLEDAndBuzzerBehaviour {

	/** 1255: To show Battery Charging Status */
	BATTERY_CHARGING_STATUS_LED,

	/**
	 * 1252: PICC Activation Status LED: To show the activation status of the PICC interface. <br/>
	 * 1255: PICC Activation Status LED: To show the activation status of the PICC interface. <br/>
	 * 1552: PICC Activation Status LED
	 */
	PICC_ACTIVATION_STATUS_LED,

	/**
	 * 1281: ICC Activation Status LED: To show the activations status of the ICC interface. <br/>
	 */
	ICC_ACTIVATION_STATUS_LED,
	/**
	 * 1255: PICC Polling Status LED: To show the PICC Polling Status. <br/>
	 * 1252: PICC Polling Status LED: To show the PICC Polling Status. <br/>
	 * 1281: PICC Polling Status LED: To show the PICC Polling Status. <br/>
	 * 1552: PICC Polling Status LED
	 */
	PICC_POLLING_STATUS_LED,

	/**
	 * 1255: Card insertion and removal events buzzers. <br/>
	 * 1252: Card Insertion and Removal Events Buzzer: To make a beep whenever a card insertion or removal event is detected (for both ICC and PICC). <br/>
	 * 1252: Card Insertion and Removal Events Buzzer: To make a beep whenever a card insertion or removal event is detected (for both ICC and PICC). <br/>
	 */
	CARD_INSERTION_AND_REMOVAL_EVENTS_BUZZER,

	/**
	 * 1255: Card Insertion Events Buzzer: To make a beep whenever a card insertion event is detected. <br/>
	 * 1552: Presence Event (Short Buzzer Beep)
	 */

	CARD_INSERTION_EVENT_BUZZER,

	/**
	 * 1255: Card Removal Events Buzzer: To make a beep whenever a card removal event is detected; needs the card insertion events
	 * buzzer (firmware 2.04.xx and later). <br/>
	 * 1552: Card Removal Event (Short Buzzer Beep)
	 */

	CARD_REMOVAL_EVENT_BUZZER,

	/**
	 * 1252: PN512 Reset Indication Buzzer: To make a beep when the PN512 is reset <br/>
	 * 1281: Contactless Chip Reset Indication Buzzer: To make a beep when the contactless chip is reset. <br/>
	 */

	CONTACTLESS_CHIP_RESET_INDICATION_BUZZER,

	/**
	 * 1281: Exclusive Mode Status Buzzer. Either ICC or PICC Interface can be activated: To make a beep when the exclusive mode is activated.<br/>
	 * 
	 */
	EXCLUSIVE_MODE_STATUS_BUZZER,

	/**
	 * 1255: Card Operation Blinking LED: To light up the LED whenever the card is being accessed. <br/>
	 * 1252: Card Operation Blinking LED: To blink the LED whenever the PICC card is being accessed. <br/>
	 * 1281: Card Operation Blinking LED: To make the LED blink whenever the card (PICC or ICC) is being accessed. <br/>
	 * 1552: Accessing(LED Fast Blinking)
	 */

	CARD_OPERATION_BLINK_LED,

	/**
	 * 1252: Color Select (GREEN): GREEN LED for status change. <br/>
	 */
	COLOR_SELECT_LED_GREEN,
	/**
	 * 1252: Color Select (RED): RED LED for status change. <br/>
	 */
	COLOR_SELECT_LED_RED,
	/**
	 * 1255: Reader Power ON Buzzer: To make a beep when the reader is powered on (firmware 2.04.xx and later). <br/>
	 * 1552: Power Up Event (Short Buzzer Beep) <br/>
	 */
	READER_POWER_ON_BUZZER;

}
