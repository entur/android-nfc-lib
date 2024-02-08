# external-websocket
Support for external NFC readers over websocket.

 * connect/disconnect 
 * borrow/unborrow reader (from a pool)
 * start/stop polling
 * tag detected
 * tag lost
 * ADPU commands

This module is primarily intended as a test artifact; it hosts a service with the above controls. A android unit test connects to the service and issue controls; the application under test in turn see

 * service started
 * reader connected
 * card present

and can interact with the card. Note that this allows for a card to be (permanently) placed on the reader and detected anew on demand. 

```java
service.connectReader(new String[]{"ACR1252", "MifareDesfireEV1"});

service.beginPolling();

// your assertions here

service.endPolling();

service.disconnectReader();

service.disconnect();
```

See [WebsocketReaderTest](../../examples/nfc-reader-android-app/src/androidTest/java/no/entur/abt/nfc/example/WebsocketReaderTest.java) with [nfc-websocket-server-java-app](examples/nfc-websocket-server-java-app) for a full example.


