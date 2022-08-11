# android-nfc-lib
This project hosts tools for working with NFC on Android. Modules:

 * [wrapper](nfc/wrapper) - wrappers for classes in the `android.nfc` package
   * for working with external and internal tags in parallel
 * [core](nfc/core) - various helpers for NFC support
   * abstract reader activities and Host Card Emulation services
 * [external](nfc/external) - basic support for external NFC readers
 * [external-acs](nfc/external-acs) - support for ACS readers external NFC readers
 * [external-minova](nfc/external-minova) - support for Minova MCR external NFC readers (over network)

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
 
# Publish
For local development (.m2)

> ./gradlew clean build publishToMavenLocal --info

For Maven central also add siging info.

> ./gradlew build publishToSonatype --info --stacktrace -Psigning.gnupg.passphrase=xxx -Psigning.gnupg.keyName=yyy

