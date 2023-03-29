package no.entur.android.nfc.external.acs.tag;


import static org.junit.Assert.assertEquals;

import org.junit.Test;
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




}
