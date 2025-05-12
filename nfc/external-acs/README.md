# external-acs
Support for various external ACS NFC readers fram ACR.

# Overview
The central component is a `Service` for handling USB connectivity. It signals the application activities using `Intent`s, using the same approach as the internal NFC: 

 * the actual 'worker' objects like `Tag` and `IsoDep` bind to a backing service, which forward commands the the actual reader / tag. 
 * when they are transported in `Intent`s (i.e. serialized), they save a reference to the service; reconnecting upon deserialization. 

The service also publishes reader connect-/disconnect-Intents with reader controls. Reader controls can be used to configure various settings, like tag polling types and intervals, beeping, lights and so on.

## Example of SAM use

``` 
// ACR 1252: Remove rubber feet and use a screwdriver to insert the SAM.
if(reader.getName().contains("1252") && reader.getNumberOfSlots() == 2) {
    try {
        byte[] power = reader.power(1, 2); // atr
        LOGGER.info("Got power response " + ByteArrayHexStringConverter.toHexString(power));

        reader.setProtocol(1, 1);

        byte[] command = ... // your code here
        byte[] transmit = reader.transmit(1, command);

        LOGGER.info("Got reader response " + ByteArrayHexStringConverter.toHexString(transmit));
    } catch(Exception e) {
        LOGGER.error("Problem talking to SAM", e);
    }
}
```