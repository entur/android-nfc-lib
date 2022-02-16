# android-nfc-lib
This project hosts tools for working with NFC on Android. Modules:

 * [core](nfc/core) - various helpers for NFC support
   * wrapper for native NFC objects for simplified testing and/or support for custom NFC services (i.e. supporting external NFC readers)
   * abstract reader activities and Host Card Emulation services
 * [external](nfc/external) - basic support for external NFC readers
 * [external-acs](nfc/external-acs) - support for ACS readers external NFC readers

# License
[European Union Public Licence v1.2](https://eupl.eu/).

# Obtain
The project is built with Gradle and is available on the central Maven repository. 

# Acknowledgements
This project includes some code from the following projects:

 * [external-nfc-api](https://github.com/skjolber/external-nfc-api)
 * [nfctools](https://github.com/grundid/nfctools) project
 * [smartrac-sdk-java-android-nfc](https://github.com/SMARTRACTECHNOLOGY-PUBLIC/smartrac-sdk-java-android-nfc)
 
 as well as drivers from ACS.
 
# Publish

> ./gradlew build publishToSonatype --info --stacktrace

