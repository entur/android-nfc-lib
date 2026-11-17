# nfc-websocket-server-java-app
Server which offers up websocket traffic to locally connected external NFC readers.

Supported readers (ACS):

 * ACR 122U, ACR 1222L, ACR 1252U, ACR 1281U, ACR 1552U
 * ACR 1251U, ACR 1255U-J1 (USB), ACR 1283L - untested with actual hardware

Other readers can be added by overriding `ExtendedCardTerminalFactory.createTerminal(..)`.

Supported Andorid tag types:

 * `IsoDep`
 * `MifareUltralight` (and `NfcA`), including NTAG - READ, WRITE and FAST_READ commands, translated to PC/SC storage card commands. 
   GET_VERSION and READ_SIG are read once when the card is detected (ACR 122U and ACR 1222L via PN532 InCommunicateThru, ACR 1252U and ACR 1552U via a PC/SC transparent session), and only for cards which support them.
   The ACR 1281U does not read GET_VERSION and READ_SIG, as its transparent session did not work reliably.
   Like with Android's own NFC reader, a card which did not respond to GET_VERSION or READ_SIG (i.e. an original MIFARE Ultralight) 
   is lost when the client sends that command (`TagLostException`), until the card is removed and presented again.

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
## ACR 122U not found (Linux)
The kernel NFC driver `pn533_usb` might claim the ACR 122U (it is based on a NXP PN532), so `pcscd` cannot open it
(`journalctl -u pcscd` shows `ACS ACR122U init failed`). Unload the kernel driver

```sh
sudo modprobe -r pn533_usb pn533 nfc
```

then reconnect the reader (or run `sudo systemctl restart pcscd`). To keep the kernel driver from claiming the reader again, blacklist it:

```sh
printf 'blacklist pn533_usb\nblacklist pn533\nblacklist nfc\n' | sudo tee /etc/modprobe.d/blacklist-pn533.conf
```
