# nfc-websocket-server-java-app
Server which offers up websocket traffic to locally connected external NFC readers.

Supported readers (ACS):

 * ACR 1252U
 * ACR 122U, ACR 1222L, ACR 1251U, ACR 1255U-J1 (USB), ACR 1281U, ACR 1283L, ACR 1552U - untested with actual hardware

Other readers can be added by overriding `ExtendedCardTerminalFactory.createTerminal(..)`.

Supported Andorid tag types:

 * `IsoDep`
 * `MifareUltralight` (and `NfcA`) - READ, WRITE and FAST_READ commands, translated to PC/SC storage card commands

## Usage
Run command

> ./gradlew runWebsocketServer

for a service on port 3001.

# Troubleshooting
## Port not released.
Run 

> ./gradlew --stop

and wait a few seconds.

## No reader found
Adjust the `sun.security.smartcardio.library` in `build.gradle` for this module.