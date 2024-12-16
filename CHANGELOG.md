# Release notes

## 2.0.7

 * Fix nullpointer on unsupported ACS readers
 * Add ACR 1552 readers
 
## 2.0.6

 * Update build
 * Bump dependencies
 * Remove desugaring

## 2.0.3 - 2.0.5

 * Add target type detector:
   * Technology type
   * UID (length, manufacturer, series)
   * Select application

## 2.0.2

 * Fix ACS USB detector problem when multiple USB devices are connect

## 2.0.1

 * Fix ACS USB read problem upon reconnect
 
## 2.0.0

 * Networked NFC reader support
 * New artifact coordinates

Note: Maintenance for 1.1.x series go into the 1.1.x branch. 

## 1.1.0
 
 * Reduced logging, moved to SLF4J.
 * Added example application for ACS.
 * `ExternalNfcServiceCallbackSupport` must now be enabled.

