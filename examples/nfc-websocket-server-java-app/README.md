# nfc-websocket-server-java-app
Server which offers up websocket traffic to locally connected external NFC readers.

Supported readers:

 * ACR 1252U

Supported Andorid tag types:

 * `IsoDep`

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