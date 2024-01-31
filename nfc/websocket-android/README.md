# external-acs
Support for various external NFC readers fram ACS.

 * ACR readers

# Overview
The central component is a `Service` for handling USB connectivity. It signals the application activities using `Intent`s, using the same approach as the internal NFC: 

 * the actual 'worker' objects like `Tag` and `IsoDep` bind to a backing service, which forward commands the the actual reader / tag. 
 * when they are transported in `Intent`s (i.e. serialized), they save a reference to the service; reconnecting upon deserialization. 

The service also publishes reader connect-/disconnect-Intents with reader controls. Reader controls can be used to configure various settings, like tag polling types and intervals, beeping, lights and so on.

