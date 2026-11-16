package no.entur.android.nfc.external.acs.tag;


import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.nfctools.api.detect.DefaultTagTypeDetector;
import org.nfctools.api.TagType;

import no.entur.android.nfc.util.ByteArrayHexStringConverter;

public class DefaultTagTypeIdentifierTest {

	private DefaultTagTypeDetector defaultTagTypeIdentifier = new DefaultTagTypeDetector<>();
	
	@Test
	public void testAtr() {
		TagType result = defaultTagTypeIdentifier.parseAtr(null, ByteArrayHexStringConverter.hexStringToByteArray("3B8F8001804F0CA0000003060300030000000068"));
		
		assertEquals(TagType.MIFARE_ULTRALIGHT, result);
	}

	@Test
	public void testAtrDesfire() {
		TagType result = defaultTagTypeIdentifier.parseAtr(null, ByteArrayHexStringConverter.hexStringToByteArray("3B8180018080"));

		assertEquals(TagType.DESFIRE_EV1, result);
	}

	@Test
	public void testAtrEmv() {
		TagType result = defaultTagTypeIdentifier.parseAtr(null, ByteArrayHexStringConverter.hexStringToByteArray("3B8F800100B85431000090000000000000000043"));

		assertEquals(TagType.ISO_DEP, result);
	}

	@Test
	public void testAtrMobile() {
		TagType result = defaultTagTypeIdentifier.parseAtr(null, ByteArrayHexStringConverter.hexStringToByteArray("3B88800100000000808175007D"));

		assertEquals(TagType.ISO_DEP, result);
	}

	@Test
	public void testAtrTruncatedTlv() {
		// historical bytes 80 05 00 00: TLV parsing runs out of bytes when reading the length
		TagType result = defaultTagTypeIdentifier.parseAtr(null, ByteArrayHexStringConverter.hexStringToByteArray("3B8480018005000080"));

		assertEquals(TagType.ISO_DEP, result);
	}

	@Test
	public void testAtrShortInitialAccessData() {
		// historical bytes 80 4F 03 A0 00 00: initial access data too short to contain a card name
		TagType result = defaultTagTypeIdentifier.parseAtr(null, ByteArrayHexStringConverter.hexStringToByteArray("3B868001804F03A000006B"));

		assertEquals(TagType.ISO_DEP, result);
	}




}
