package no.entur.abt.nfc.example.ultralight;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Latest scan of a tag (by UID) on the phone and on each external reader.
 */

public class TagRecord {

	private final String uid;
	private final int number;
	private ScanResult phone;
	private final Map<String, ScanResult> externals = new LinkedHashMap<>();

	public TagRecord(String uid, int number) {
		this.uid = uid;
		this.number = number;
	}

	public String getUid() {
		return uid;
	}

	public int getNumber() {
		return number;
	}

	public ScanResult getPhone() {
		return phone;
	}

	/**
	 * @param readerKey reader key
	 * @return the latest scan on that external reader, or null
	 */

	public ScanResult getExternal(String readerKey) {
		return externals.get(readerKey);
	}

	public List<ScanResult> getExternals() {
		return new ArrayList<>(externals.values());
	}

	public List<ScanResult> getScans() {
		List<ScanResult> scans = new ArrayList<>();
		if (phone != null) {
			scans.add(phone);
		}
		scans.addAll(externals.values());
		return scans;
	}

	public void set(ScanResult result) {
		if (result.source == ScanResult.Source.PHONE) {
			this.phone = result;
		} else {
			externals.put(result.readerKey, result);
		}
	}

	/**
	 * The tag family, from the phone (the reference) if possible.
	 *
	 * @return family, never null
	 */

	public TagFamily getFamily() {
		if (phone != null && phone.family != null) {
			return phone.family;
		}
		for (ScanResult external : externals.values()) {
			if (external.family != null) {
				return external.family;
			}
		}
		return TagFamily.UNKNOWN;
	}

	/**
	 * @return true if the family is not (yet) confirmed by the phone
	 */

	public boolean isFamilyProvisional() {
		return phone == null || phone.family == null;
	}
}
