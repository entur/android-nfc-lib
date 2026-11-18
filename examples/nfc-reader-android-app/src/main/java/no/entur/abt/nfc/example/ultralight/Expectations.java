package no.entur.abt.nfc.example.ultralight;

/**
 * Expected outcome of each check, from the tag family (as identified by the phone) and the reader.
 */

public final class Expectations {

	private Expectations() {
	}

	/**
	 * @param check check
	 * @param family tag family
	 * @param capability reader
	 * @return TRUE if the check should succeed, FALSE if it should fail, null if there is no expectation
	 */

	public static Boolean expected(Check check, TagFamily family, ReaderCapability capability) {
		switch (check) {
		case READ:
		case UID:
		case BCC:
		case MEMORY:
			return Boolean.TRUE;
		case GET_VERSION:
		case NFCA_GET_VERSION:
			return raw(capability.getSupport(check), family.getGetVersion());
		case READ_SIG:
		case NFCA_FAST_READ:
			return raw(capability.getSupport(check), family.getReadSignatureAndFastRead());
		case AUTH1:
			if (capability == ReaderCapability.PHONE) {
				// the phone result identifies the family, so it cannot be judged
				return null;
			}
			return raw(capability.getSupport(check), family.getUltralightCAuthentication());
		default:
			return null;
		}
	}

	private static Boolean raw(ReaderCapability.RawSupport support, Boolean supportedByTag) {
		switch (support) {
		case NOT_SUPPORTED:
			// the library does not send this command to the tag for this reader
			return Boolean.FALSE;
		case NOT_KNOWN:
			return null;
		default:
			return supportedByTag;
		}
	}

	/**
	 * @return true if the outcome contradicts the expectation
	 */

	public static boolean isUnexpected(Outcome outcome, Boolean expected) {
		return outcome != null && outcome.isRun() && expected != null && outcome.isOk() != expected;
	}

	public static boolean isUnverified(Check check, ReaderCapability capability) {
		return capability.getSupport(check) == ReaderCapability.RawSupport.UNVERIFIED;
	}

	/**
	 * Cell text: ok / fail as expected, !ok / !fail not as expected, ok* / fail* as expected for an unverified reader, ok? / fail? no expectation.
	 */

	public static String cell(Outcome outcome, Boolean expected, boolean unverified) {
		if (outcome == null) {
			return "-";
		}
		switch (outcome.getStatus()) {
		case LOST:
			return "lost";
		case SKIPPED:
			return "skip";
		default:
			break;
		}
		String text = outcome.isOk() ? "ok" : "fail";
		if (expected == null) {
			return text + "?";
		}
		if (outcome.isOk() != expected) {
			return "!" + text;
		}
		if (unverified) {
			return text + "*";
		}
		return text;
	}

	public static String expectedText(Boolean expected) {
		if (expected == null) {
			return "-";
		}
		return expected ? "ok" : "fail";
	}
}
