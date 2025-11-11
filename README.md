[![Maven Central](https://img.shields.io/maven-central/v/no.entur.android.nfc/core.svg)](https://mvnrepository.com/artifact/no.entur.android.nfc)


# android-nfc-lib
This project hosts tools for working with NFC on Android. Modules:

 * [wrapper](nfc/wrapper) - wrappers for classes in the `android.nfc` package
   * for working with external and internal tags in parallel
 * [wrapper-utils](nfc/wrapper-utils) - wrapper-related utilities
 * [wrapper-test](nfc/wrapper-test) - test support
 * [core](nfc/core) - various helpers for NFC support
   * abstract reader activities and Host Card Emulation services
 * [external](nfc/external) - basic support for external NFC readers
 * [external-acs](nfc/external-acs) - support for ACS readers external NFC readers
 * [external-minova](nfc/external-minova) - support for Minova MCR external NFC readers (over network)
 * [external-websocket](nfc/external-websocket) - support for interacting with a pool of external readers over websocket
   * add NFC support to devices without NFC chips (i.e. emulator / Android TV / certain tablets), and/or 
   * programmatically interact with multiple cards/readers during testing, using fine-grained controls:
     * connect/disconnect reader
     * start/stop polling
       * trigger tag scans even if the card is statically placed on the reader

# License
[European Union Public Licence v1.2](https://eupl.eu/).

# Obtain
The project is built with Gradle and is [available](https://mvnrepository.com/artifact/no.entur.android.nfc) on the central Maven repository. 

# Acknowledgements
This project includes some code from the following projects:

 * [external-nfc-api](https://github.com/skjolber/external-nfc-api)
 * [nfctools](https://github.com/grundid/nfctools) project
 * [smartrac-sdk-java-android-nfc](https://github.com/SMARTRACTECHNOLOGY-PUBLIC/smartrac-sdk-java-android-nfc)
 
as well as drivers from ACS.

# Troubleshooting

 * For [external-websocket](nfc/external-websocket) use a JVM which bundles `smartcardio` packages

# Publish
For local development (.m2)

> ./gradlew clean build publishToMavenLocal --info

For Maven central also add siging info.

> ./gradlew build publishToSonatype closeAndReleaseStagingRepositories --info --stacktrace -Psigning.gnupg.passphrase=xxx -Psigning.gnupg.keyName=yyy

# History
See [CHANGELOG](CHANGELOG.md).
