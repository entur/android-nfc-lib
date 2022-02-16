# core
This module contains basic constructs for implementing NFC support in an Android application.

Features:

 * Wrapper for native NFC objects for simplified testing and/or support for custom NFC services (i.e. supporting external NFC readers)
   * So the library avoids using NFC-related classes which were marked as `secret` or `hidden` in Android 10
 * Abstract activities for reading NFC cards
 * Various HCE support classes
   * Grab interface / be the preferred app for an AID when app is open
   * Send / recieve over HCE
   * Command protocol stubs
   

