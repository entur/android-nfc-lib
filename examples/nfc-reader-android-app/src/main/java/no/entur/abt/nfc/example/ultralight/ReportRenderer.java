package no.entur.abt.nfc.example.ultralight;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import no.entur.android.nfc.util.ByteArrayHexStringConverter;

/**
 * Renders the results as Markdown tables, padded so that they are also readable as plain monospace text.
 * One column for the phone (the reference) and one for each external reader.
 */

public class ReportRenderer {

	public static final String LEGEND = "Cells: ok / fail = as expected, !ok / !fail = NOT as expected, ok* / fail* = as expected but unverified reader, "
			+ "ok? / fail? = no expectation, lost = tag removed too early (tap again), skip = not run, - = not applicable. "
			+ "Expected is for the tag; readers which do not send a command to the tag are expected to fail it.";

	private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.ROOT);

	/**
	 * @param header header
	 * @param records tags
	 * @param readers external readers (key, label)
	 * @param events events
	 * @return report
	 */

	public String render(String header, List<TagRecord> records, Map<String, String> readers, List<String> events) {
		StringBuilder builder = new StringBuilder();
		builder.append("## ").append(header).append("\n\n");
		builder.append("### Tag types\n\n");
		builder.append(types(records)).append("\n");
		builder.append("### Tags\n\n");
		builder.append(renderTags(records, readers, events));
		return builder.toString();
	}

	/**
	 * @return the per tag part of the report
	 */

	public String renderTags(List<TagRecord> records, Map<String, String> readers, List<String> events) {
		StringBuilder builder = new StringBuilder();
		builder.append(LEGEND).append("\n\n");

		if (records.isEmpty()) {
			builder.append("No tags scanned yet.\n");
		} else {
			builder.append(summary(records, readers)).append("\n");
			for (TagRecord record : records) {
				builder.append(details(record, readers)).append("\n");
			}
		}
		if (!events.isEmpty()) {
			builder.append("### Events\n\n");
			for (String event : events) {
				builder.append("- ").append(event).append("\n");
			}
		}
		return builder.toString();
	}

	public String summary(List<TagRecord> records, Map<String, String> readers) {
		List<String[]> rows = new ArrayList<>();

		List<String> header = new ArrayList<>(Arrays.asList("#", "UID", "Tag", "Phone"));
		header.addAll(readers.values());
		header.add("getType()");
		header.add("Result");
		rows.add(header.toArray(new String[0]));

		for (TagRecord record : records) {
			List<String> row = new ArrayList<>();
			row.add(Integer.toString(record.getNumber()));
			row.add(record.getUid());
			row.add(familyText(record));
			row.add(summaryCell(record, record.getPhone()));
			for (String readerKey : readers.keySet()) {
				row.add(summaryCell(record, record.getExternal(readerKey)));
			}
			row.add(typeSummary(record, readers));
			row.add(result(record));
			rows.add(row.toArray(new String[0]));
		}
		return table(rows);
	}

	public String details(TagRecord record, Map<String, String> readers) {
		StringBuilder builder = new StringBuilder();
		TagFamily family = record.getFamily();
		ScanResult phone = record.getPhone();

		builder.append("### #").append(record.getNumber()).append(" ").append(record.getUid()).append(": ").append(familyText(record)).append("\n\n");
		builder.append("- Family: ").append(familySource(record)).append("\n");
		if (phone != null) {
			builder.append("- Phone: ").append(scanText(phone)).append("\n");
		}
		List<ScanResult> columns = new ArrayList<>();
		for (String readerKey : readers.keySet()) {
			ScanResult external = record.getExternal(readerKey);
			columns.add(external);
			if (external != null) {
				builder.append("- ").append(external.readerLabel).append(": ").append(scanText(external)).append("\n");
			}
		}
		builder.append("\n");

		List<String[]> rows = new ArrayList<>();
		List<String> header = new ArrayList<>(Arrays.asList("Check", "Expected", "Phone"));
		header.addAll(readers.values());
		rows.add(header.toArray(new String[0]));

		for (Check check : Check.values()) {
			List<String> row = new ArrayList<>();
			String label = check.getLabel();
			if (check == Check.MEMORY) {
				label = "READ all " + family.getReadablePages() + " pages";
			}
			row.add(label);
			row.add(Expectations.expectedText(Expectations.expected(check, family, ReaderCapability.PHONE)));
			row.add(cell(record, phone, check));
			for (ScanResult external : columns) {
				row.add(cell(record, external, check));
			}
			rows.add(row.toArray(new String[0]));
		}

		List<String> typeRow = new ArrayList<>(Arrays.asList("getType()", "same as phone", typeText(phone)));
		List<String> memoryRow = new ArrayList<>(Arrays.asList("Memory equals phone", "ok", "-"));
		for (ScanResult external : columns) {
			typeRow.add(externalTypeCell(record, external));
			memoryRow.add(memoryCell(record, external));
		}
		rows.add(typeRow.toArray(new String[0]));
		rows.add(memoryRow.toArray(new String[0]));

		builder.append(table(rows));

		List<String> notes = notes(record);
		if (!notes.isEmpty()) {
			builder.append("\n");
			for (String note : notes) {
				builder.append("- ").append(note).append("\n");
			}
		}
		return builder.toString();
	}

	private List<String> notes(TagRecord record) {
		List<String> notes = new ArrayList<>();
		ScanResult phone = record.getPhone();
		if (phone != null) {
			NtagMirror mirror = NtagMirror.counter(record.getFamily(), phone);
			if (mirror != null) {
				notes.add(mirror + ": changes on every tap, not compared");
			}
		}
		addNotes(notes, "Phone", phone);
		for (ScanResult external : record.getExternals()) {
			addNotes(notes, external.readerLabel, external);
			if (isTypeDifferent(record, external)) {
				notes.add(external.readerLabel + " getType() differs from the phone. Pages 2-4 phone: " + pages(phone, 2, 3) + ", " + external.readerLabel + ": "
						+ pages(external, 2, 3));
			}
			if (phone != null) {
				String memory = memoryDifferences(record, phone, external);
				if (memory != null) {
					notes.add(external.readerLabel + " memory differs from the phone: " + memory);
				}
			}
		}
		return notes;
	}

	private void addNotes(List<String> notes, String name, ScanResult result) {
		if (result == null) {
			return;
		}
		if (result.error != null) {
			notes.add(name + ": " + result.error);
		}
		for (Check check : Check.values()) {
			Outcome outcome = result.get(check);
			if (outcome != null && !outcome.isOk() && outcome.getDetail() != null) {
				notes.add(name + " " + check.getLabel() + ": " + outcome.getDetail());
			}
		}
	}

	private String pages(ScanResult result, int start, int count) {
		StringBuilder builder = new StringBuilder();
		for (int i = start; i < start + count; i++) {
			byte[] page = result.getPage(i);
			if (builder.length() > 0) {
				builder.append(' ');
			}
			builder.append(page != null ? ByteArrayHexStringConverter.toHexString(page) : "-");
		}
		return builder.toString();
	}

	private String scanText(ScanResult result) {
		StringBuilder builder = new StringBuilder();
		builder.append(timeFormat.format(new Date(result.time)));
		if (result.source == ScanResult.Source.EXTERNAL) {
			if (result.readerName != null) {
				builder.append(", ").append(result.readerName);
			}
			builder.append(" (").append(result.capability.getLabel()).append(")");
		}
		if (result.technologies != null) {
			builder.append(", ").append(result.technologies);
		}
		if (result.ultralightTypeExtra != null) {
			builder.append(", EXTRA_ULTRALIGHT_TYPE ").append(result.ultralightTypeExtra);
		}
		if (result.lost) {
			builder.append(", tag lost - tap again");
		}
		return builder.toString();
	}

	private String familyText(TagRecord record) {
		TagFamily family = record.getFamily();
		if (!family.isKnown()) {
			return "unknown";
		}
		if (record.isFamilyProvisional()) {
			return family.getLabel() + " (provisional)";
		}
		return family.getLabel();
	}

	private String familySource(TagRecord record) {
		ScanResult phone = record.getPhone();
		if (phone != null && phone.family != null) {
			return describeFamily(phone, "phone");
		}
		for (ScanResult external : record.getExternals()) {
			if (external.family != null) {
				return describeFamily(external, external.readerLabel) + ", provisional until scanned on the phone";
			}
		}
		if (phone == null) {
			return "unknown, scan on the phone";
		}
		return "unknown, phone could not identify the tag";
	}

	private String describeFamily(ScanResult result, String source) {
		if (result.version != null) {
			return result.family.getLabel() + " from " + source + " GET_VERSION " + ByteArrayHexStringConverter.toHexString(result.version);
		}
		if (result.family == TagFamily.ULTRALIGHT_C) {
			return result.family.getLabel() + " from " + source + " AUTHENTICATE response";
		}
		return result.family.getLabel() + " from " + source + " (no GET_VERSION, no Ultralight C AUTHENTICATE)";
	}

	private String cell(TagRecord record, ScanResult result, Check check) {
		if (result == null) {
			return "";
		}
		Outcome outcome = result.get(check);
		Boolean expected = Expectations.expected(check, record.getFamily(), result.capability);
		String text = Expectations.cell(outcome, expected, Expectations.isUnverified(check, result.capability));
		if (check == Check.MEMORY && outcome != null && outcome.isRun()) {
			text = text + " (" + result.pagesRead + "/" + result.pagesExpected + ")";
		}
		return text;
	}

	private String typeText(ScanResult result) {
		if (result == null) {
			return "";
		}
		return typeName(result.type);
	}

	private String externalTypeCell(TagRecord record, ScanResult external) {
		if (external == null) {
			return "";
		}
		String text = typeName(external.type);
		if (record.getPhone() == null) {
			return text + "?";
		}
		if (isTypeDifferent(record, external)) {
			return "!" + text;
		}
		return text;
	}

	private String memoryCell(TagRecord record, ScanResult external) {
		if (external == null) {
			return "";
		}
		ScanResult phone = record.getPhone();
		if (phone == null) {
			return "-";
		}
		int pages = Math.min(phone.pagesRead, external.pagesRead);
		if (pages == 0) {
			return "-";
		}
		int page = firstDifferentPage(record, phone, external);
		if (page != -1) {
			return "!fail (page " + page + ")";
		}
		return "ok (" + pages + " pages)";
	}

	/**
	 * @return the differing pages (up to 8, phone / external), or null if the same
	 */

	static String memoryDifferences(TagRecord record, ScanResult phone, ScanResult external) {
		NtagMirror mirror = NtagMirror.counter(record.getFamily(), phone);
		int pages = Math.min(phone.pagesRead, external.pagesRead);
		StringBuilder builder = new StringBuilder();
		int count = 0;
		for (int page = 0; page < pages; page++) {
			if (!isSamePage(mirror, page, phone, external)) {
				if (count < 8) {
					if (builder.length() > 0) {
						builder.append(", ");
					}
					builder.append("page ").append(page).append(' ').append(ByteArrayHexStringConverter.toHexString(phone.getPage(page))).append(" / ")
							.append(ByteArrayHexStringConverter.toHexString(external.getPage(page)));
				}
				count++;
			}
		}
		if (count == 0) {
			return null;
		}
		return count + " of " + pages + " pages (phone / reader): " + builder + (count > 8 ? ", ..." : "");
	}

	/**
	 * Compare a page, except for the NFC counter mirror bytes (the counter is incremented on each tap).
	 */

	private static boolean isSamePage(NtagMirror mirror, int page, ScanResult phone, ScanResult external) {
		byte[] a = phone.getPage(page);
		byte[] b = external.getPage(page);
		for (int i = 0; i < 4; i++) {
			if (a[i] != b[i] && (mirror == null || !mirror.contains(page * 4 + i))) {
				return false;
			}
		}
		return true;
	}

	private static int firstDifferentPage(TagRecord record, ScanResult phone, ScanResult external) {
		NtagMirror mirror = NtagMirror.counter(record.getFamily(), phone);
		int pages = Math.min(phone.pagesRead, external.pagesRead);
		for (int page = 0; page < pages; page++) {
			if (!isSamePage(mirror, page, phone, external)) {
				return page;
			}
		}
		return -1;
	}

	private static boolean isTypeDifferent(TagRecord record, ScanResult external) {
		ScanResult phone = record.getPhone();
		if (phone == null || external == null) {
			return false;
		}
		return phone.type == null ? external.type != null : !phone.type.equals(external.type);
	}

	/**
	 * @return number of checks which were not as expected for one scan
	 */

	public static int unexpected(TagRecord record, ScanResult result) {
		if (result == null) {
			return 0;
		}
		int count = 0;
		for (Check check : Check.values()) {
			if (Expectations.isUnexpected(result.get(check), Expectations.expected(check, record.getFamily(), result.capability))) {
				count++;
			}
		}
		if (result.source == ScanResult.Source.EXTERNAL) {
			count += mismatches(record, result).size();
		}
		return count;
	}

	/**
	 * Compare an external scan with the phone scan of the same tag.
	 *
	 * @return differences, empty if they correspond (or there is no phone scan)
	 */

	public static List<String> mismatches(TagRecord record, ScanResult external) {
		List<String> mismatches = new ArrayList<>();
		ScanResult phone = record.getPhone();
		if (phone == null || external == null || external.source != ScanResult.Source.EXTERNAL) {
			return mismatches;
		}
		if (isTypeDifferent(record, external)) {
			mismatches.add("getType() " + typeName(external.type) + ", phone " + typeName(phone.type));
		}
		int page = firstDifferentPage(record, phone, external);
		if (page != -1) {
			mismatches.add("memory differs from the phone at page " + page);
		}
		compare(mismatches, phone, external, Check.GET_VERSION, "GET_VERSION differs from the phone");
		compare(mismatches, phone, external, Check.NFCA_GET_VERSION, "GET_VERSION (NfcA) differs from the phone");
		compare(mismatches, phone, external, Check.READ_SIG, "READ_SIG signature differs from the phone");
		return mismatches;
	}

	private static void compare(List<String> mismatches, ScanResult phone, ScanResult external, Check check, String message) {
		Outcome a = phone.get(check);
		Outcome b = external.get(check);
		if (a != null && b != null && a.isOk() && b.isOk() && a.getDetail() != null && !a.getDetail().equals(b.getDetail())) {
			mismatches.add(message);
		}
	}

	/**
	 * Three lights per tag type, as a table.
	 */

	public String types(List<TagRecord> records) {
		List<TypeStatus> statuses = TypeStatus.compute(records);

		List<String[]> rows = new ArrayList<>();
		rows.add(new String[] { "Tag type", "Phone", "External", "Match", "Tags", "Readers" });
		List<String> problems = new ArrayList<>();
		for (TypeStatus status : statuses) {
			rows.add(new String[] { status.getFamily().getLabel(), lightText(status.getPhone()), lightText(status.getExternal()), lightText(status.getMatch()),
					Integer.toString(status.getTags()), join(status.getReaders()) });
			problems.addAll(status.getProblems());
		}
		StringBuilder builder = new StringBuilder();
		builder.append(table(rows));
		if (!problems.isEmpty()) {
			builder.append("\n");
			for (String problem : problems) {
				builder.append("- ").append(problem).append("\n");
			}
		}
		return builder.toString();
	}

	public static String join(Iterable<String> values) {
		StringBuilder builder = new StringBuilder();
		for (String value : values) {
			if (builder.length() > 0) {
				builder.append(", ");
			}
			builder.append(value);
		}
		return builder.toString();
	}

	public static String lightText(TypeStatus.Light light) {
		switch (light) {
		case PASS:
			return "PASS";
		case FAIL:
			return "FAIL";
		case INCOMPLETE:
			return "INCOMPLETE";
		default:
			return "-";
		}
	}

	private String summaryCell(TagRecord record, ScanResult result) {
		if (result == null) {
			return "-";
		}
		if (result.lost) {
			return "lost";
		}
		int unexpected = unexpected(record, result);
		Outcome read = result.get(Check.READ);
		if (read == null || !read.isOk()) {
			return unexpected == 0 ? "not read" : "not read, " + unexpected + " unexp.";
		}
		return unexpected == 0 ? "ok" : unexpected + " unexp.";
	}

	private String typeSummary(TagRecord record, Map<String, String> readers) {
		StringBuilder builder = new StringBuilder();
		boolean different = false;
		builder.append(record.getPhone() != null ? shortTypeName(record.getPhone().type) : "-");
		for (String readerKey : readers.keySet()) {
			ScanResult external = record.getExternal(readerKey);
			builder.append('/');
			builder.append(external != null ? shortTypeName(external.type) : "-");
			different |= isTypeDifferent(record, external);
		}
		return different ? "!" + builder : builder.toString();
	}

	public static String result(TagRecord record) {
		List<ScanResult> scans = record.getScans();
		for (ScanResult scan : scans) {
			if (scan.lost) {
				return "incomplete";
			}
		}
		int unexpected = 0;
		for (ScanResult scan : scans) {
			unexpected += unexpected(record, scan);
		}
		if (unexpected > 0) {
			return unexpected + " unexpected";
		}
		if (record.getPhone() == null) {
			return "scan on phone";
		}
		if (record.getExternals().isEmpty()) {
			return "scan on external";
		}
		return "OK";
	}

	/**
	 * @param readerName reader name, i.e. ACS ACR122U PICC Interface
	 * @return short name, i.e. ACR122U
	 */

	public static String shortReaderName(String readerName) {
		for (String token : readerName.split(" ")) {
			if (token.startsWith("ACR")) {
				return token;
			}
		}
		return readerName;
	}

	public static String typeName(Integer type) {
		if (type == null) {
			return "-";
		}
		switch (type) {
		case 1:
			return "1 Ultralight";
		case 2:
			return "2 Ultralight C";
		default:
			return type + " unknown";
		}
	}

	public static String shortTypeName(Integer type) {
		if (type == null) {
			return "-";
		}
		switch (type) {
		case 1:
			return "UL";
		case 2:
			return "C";
		default:
			return "?";
		}
	}

	public static String table(List<String[]> rows) {
		int columns = rows.get(0).length;
		int[] widths = new int[columns];
		for (String[] row : rows) {
			for (int i = 0; i < columns; i++) {
				widths[i] = Math.max(widths[i], row[i].length());
			}
		}
		StringBuilder builder = new StringBuilder();
		for (int r = 0; r < rows.size(); r++) {
			appendRow(builder, rows.get(r), widths);
			if (r == 0) {
				String[] separator = new String[columns];
				for (int i = 0; i < columns; i++) {
					separator[i] = repeat('-', widths[i]);
				}
				appendRow(builder, separator, widths);
			}
		}
		return builder.toString();
	}

	private static void appendRow(StringBuilder builder, String[] row, int[] widths) {
		builder.append('|');
		for (int i = 0; i < row.length; i++) {
			builder.append(' ').append(row[i]).append(repeat(' ', widths[i] - row[i].length())).append(" |");
		}
		builder.append('\n');
	}

	private static String repeat(char c, int count) {
		char[] chars = new char[Math.max(0, count)];
		Arrays.fill(chars, c);
		return new String(chars);
	}
}
