# nfc-reader-app
Example application for testing ACR readers.

 * service start / stop
 * reader connect / disconnect
 * tag present / lost

## Websocket
Websocket support is included as a __test artifact__. 

When running `WebsocketReaderTest` unit test on local emulator, first do

> adb reverse tcp:3001 tcp:3001
 
