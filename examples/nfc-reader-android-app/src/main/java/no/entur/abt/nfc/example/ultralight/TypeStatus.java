package no.entur.abt.nfc.example.ultralight;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Three lights per tag type: phone, external readers, and whether phone and external readers correspond.
 */

public class TypeStatus {

	public enum Light {
		/** not tested */
		NONE,
		PASS,
		/** tag lost during the scan, or not identified by the phone */
		INCOMPLETE,
		FAIL;

		public Light worst(Light other) {
			return other.ordinal() > ordinal() ? other : this;
		}
	}

	private final TagFamily family;
	private Light phone = Light.NONE;
	private Light external = Light.NONE;
	private Light match = Light.NONE;
	private int tags;
	private final Set<String> readers = new LinkedHashSet<>();
	private final List<String> problems = new ArrayList<>();

	public TypeStatus(TagFamily family) {
		this.family = family;
	}

	/**
	 * @param records scanned tags
	 * @return status for each listed tag type, plus any other type which has been scanned
	 */

	public static List<TypeStatus> compute(List<TagRecord> records) {
		Map<TagFamily, TypeStatus> statuses = new EnumMap<>(TagFamily.class);
		for (TagFamily family : TagFamily.values()) {
			if (family.isListed()) {
				statuses.put(family, new TypeStatus(family));
			}
		}
		for (TagRecord record : records) {
			TagFamily family = record.getFamily();
			TypeStatus status = statuses.get(family);
			if (status == null) {
				status = new TypeStatus(family);
				statuses.put(family, status);
			}
			status.add(record);
		}
		return new ArrayList<>(statuses.values());
	}

	private void add(TagRecord record) {
		tags++;

		ScanResult phoneScan = record.getPhone();
		if (phoneScan != null) {
			Light light = checks(record, phoneScan, "Phone");
			if (!family.isKnown() && light == Light.PASS) {
				// the phone could not identify the tag
				light = Light.INCOMPLETE;
			}
			phone = phone.worst(light);
		}
		for (ScanResult externalScan : record.getExternals()) {
			readers.add(externalScan.readerLabel);
			external = external.worst(checks(record, externalScan, externalScan.readerLabel));

			if (phoneScan != null) {
				if (phoneScan.lost || externalScan.lost) {
					match = match.worst(Light.INCOMPLETE);
				} else {
					List<String> mismatches = ReportRenderer.mismatches(record, externalScan);
					for (String mismatch : mismatches) {
						problems.add(record.getUid() + " " + externalScan.readerLabel + ": " + mismatch);
					}
					match = match.worst(mismatches.isEmpty() ? Light.PASS : Light.FAIL);
				}
			}
		}
	}

	private Light checks(TagRecord record, ScanResult result, String name) {
		if (result.lost) {
			problems.add(record.getUid() + " " + name + ": tag lost, tap again");
			return Light.INCOMPLETE;
		}
		boolean unexpected = false;
		for (Check check : Check.values()) {
			Outcome outcome = result.get(check);
			Boolean expected = Expectations.expected(check, record.getFamily(), result.capability);
			if (Expectations.isUnexpected(outcome, expected)) {
				problems.add(record.getUid() + " " + name + ": " + check.getLabel() + " " + Expectations.cell(outcome, expected, false));
				unexpected = true;
			}
		}
		return unexpected ? Light.FAIL : Light.PASS;
	}

	public TagFamily getFamily() {
		return family;
	}

	public Light getPhone() {
		return phone;
	}

	public Light getExternal() {
		return external;
	}

	public Light getMatch() {
		return match;
	}

	public int getTags() {
		return tags;
	}

	public Set<String> getReaders() {
		return readers;
	}

	public List<String> getProblems() {
		return problems;
	}
}
