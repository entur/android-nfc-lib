package no.entur.abt.nfc.example.ultralight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.entur.android.nfc.util.ByteArrayHexStringConverter;

/**
 * Application scoped scan results, so that they survive activity recreation.
 */

public class UltralightResults {

	private static final UltralightResults INSTANCE = new UltralightResults();

	public static UltralightResults getInstance() {
		return INSTANCE;
	}

	private final Map<String, TagRecord> records = new LinkedHashMap<>();
	private final List<String> events = new ArrayList<>();
	/** external readers (key, label), in the order they were first used */
	private final Map<String, String> readers = new LinkedHashMap<>();
	/** reader id to name, from reader opened broadcasts; kept when the reader closes */
	private final Map<String, String> readerNames = new LinkedHashMap<>();
	/** reader ids of open readers */
	private final Set<String> openReaders = new LinkedHashSet<>();
	/** reader key to column title, unique per reader */
	private final Map<String, String> labels = new HashMap<>();

	public static String key(byte[] uid) {
		if (uid == null || uid.length == 0) {
			return "(no UID)";
		}
		return ByteArrayHexStringConverter.toHexString(uid);
	}

	public synchronized TagRecord add(ScanResult result) {
		String key = key(result.uid);
		TagRecord record = records.get(key);
		if (record == null) {
			record = new TagRecord(key, records.size() + 1);
			records.put(key, record);
		}
		record.set(result);
		if (result.source == ScanResult.Source.EXTERNAL) {
			String label = readers.get(result.readerKey);
			// replace a placeholder label (the reader key) once the reader name is known
			if (label == null || label.equals(result.readerKey)) {
				readers.put(result.readerKey, result.readerLabel);
			}
		}
		return record;
	}

	/**
	 * @return external readers (key, label), in the order they were first used
	 */

	public synchronized Map<String, String> getReaders() {
		return new LinkedHashMap<>(readers);
	}

	/**
	 * @param uid tag UID
	 * @return the tag family as identified by the phone, or null
	 */

	public synchronized TagFamily getReferenceFamily(byte[] uid) {
		TagRecord record = records.get(key(uid));
		if (record != null && !record.isFamilyProvisional()) {
			return record.getFamily();
		}
		return null;
	}

	public synchronized List<TagRecord> getRecords() {
		return new ArrayList<>(records.values());
	}

	public synchronized void addEvent(String event) {
		events.add(event);
	}

	public synchronized List<String> getEvents() {
		return new ArrayList<>(events);
	}

	public synchronized void readerOpened(String readerId, String name) {
		readerNames.put(readerId, name);
		openReaders.add(readerId);
	}

	public synchronized void readerClosed(String readerId) {
		openReaders.remove(readerId);
	}

	public synchronized String getReaderName(String readerId) {
		return readerNames.get(readerId);
	}

	/**
	 * @return open readers (id, name)
	 */

	public synchronized Map<String, String> getOpenReaders() {
		Map<String, String> open = new LinkedHashMap<>();
		for (String readerId : openReaders) {
			open.put(readerId, readerNames.get(readerId));
		}
		return open;
	}

	/**
	 * @return a column title which is unique per reader
	 */

	public synchronized String label(String readerKey, String preferred) {
		String label = labels.get(readerKey);
		if (label != null) {
			return label;
		}
		label = preferred;
		int count = 2;
		while (labels.containsValue(label)) {
			label = preferred + " " + count++;
		}
		labels.put(readerKey, label);
		return label;
	}

	public synchronized void clear() {
		records.clear();
		events.clear();
		readers.clear();
	}
}
