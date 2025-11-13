# mqtt-messages
DTOs for JSON text messages over MQTT

## Reader

 * discovery support generic topic
 * status polling specific topic (for specific reader)

## Tag 

 * generic topic for all readers
     * message contains reader identifier
     * only some readers need ADPU exchange

## ADPU

 * transmit to specific topic (for specific reader)
 * receive from generic topic
   * message contains per-command identifier

