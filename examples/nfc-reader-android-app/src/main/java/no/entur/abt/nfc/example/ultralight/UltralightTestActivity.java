package no.entur.abt.nfc.example.ultralight;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import no.entur.abt.nfc.example.MainApplication;
import no.entur.abt.nfc.example.R;
import no.entur.abt.nfc.example.utils.ParcelableExtraUtils;
import no.entur.android.nfc.NfcReaderCallbackSupport;
import no.entur.android.nfc.external.ExternalNfcReader;
import no.entur.android.nfc.external.ExternalNfcReaderCallback;
import no.entur.android.nfc.external.ExternalNfcReaderCallbackSupport;
import no.entur.android.nfc.external.ExternalNfcTagCallback;
import no.entur.android.nfc.external.ExternalNfcTagCallbackSupport;
import no.entur.android.nfc.external.acs.reader.AcrReader;
import no.entur.android.nfc.external.hid.card.Atr210NfcTagCallback;
import no.entur.android.nfc.wrapper.Tag;
import no.entur.android.nfc.wrapper.tech.MifareUltralight;

/**
 * Scan MIFARE Ultralight / NTAG tags on the phone and on any external reader (ACS USB, HID ATR210 via MQTT, WebSocket),
 * and show which commands work as expected. Read-only.
 */

public class UltralightTestActivity extends AppCompatActivity implements ExternalNfcTagCallback, ExternalNfcReaderCallback {

	private static final Logger LOGGER = LoggerFactory.getLogger(UltralightTestActivity.class);

	public static final String LOG_TAG = "UltralightTest";

	private static final String PHONE_KEY = "phone";
	private static final long IGNORE_REPEAT_MILLIS = 3000;
	/** continue a paused phone scan if the tag is discovered again within this time */
	private static final long RESUME_MILLIS = 15000;

	private final UltralightResults results = UltralightResults.getInstance();
	private final UltralightTagTester tester = new UltralightTagTester();
	private final ReportRenderer renderer = new ReportRenderer();
	private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.ROOT);

	private Executor executor;

	private NfcReaderCallbackSupport nfcReaderCallbackSupport;
	private ExternalNfcTagCallbackSupport externalNfcTagCallbackSupport;
	private ExternalNfcReaderCallbackSupport externalNfcReaderCallbackSupport;

	private TextView phoneStatus;
	private TextView externalStatus;
	private TextView readersStatus;
	private LinearLayout types;
	private TextView report;

	/** phone scans waiting for the tag to be discovered again (the phone drops a tag which does not answer), by UID */
	private final Map<String, UltralightTagTester.Session> paused = new ConcurrentHashMap<>();

	/** reader key, i.e. phone or an external reader, which is currently scanning */
	private final Map<String, Boolean> busy = new ConcurrentHashMap<>();
	/** reader key + UID to time of last completed scan */
	private final Map<String, Long> lastScan = new ConcurrentHashMap<>();

	private final BroadcastReceiver techDiscoveredReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String reader = describeReader(intent);
			results.addEvent(timeFormat.format(new Date()) + " " + reader + ": TECH_DISCOVERED, the reader saw a tag but did not deliver it");
			runOnUiThread(() -> {
				externalStatus.setText(getString(R.string.ultralight_test_tech_discovered, reader));
				render();
			});
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.activity_ultralight_test);

		phoneStatus = findViewById(R.id.ultralightPhoneStatus);
		externalStatus = findViewById(R.id.ultralightExternalStatus);
		readersStatus = findViewById(R.id.ultralightReadersStatus);
		types = findViewById(R.id.ultralightTypes);
		report = findViewById(R.id.ultralightReport);

		Button copy = findViewById(R.id.ultralightCopy);
		copy.setOnClickListener((v) -> copy());

		Button clear = findViewById(R.id.ultralightClear);
		clear.setOnClickListener((v) -> {
			results.clear();
			lastScan.clear();
			render();
		});

		MainApplication mainApplication = (MainApplication) getApplication();
		executor = mainApplication.getThreadPoolExecutor();

		boolean receiverExported = true;

		externalNfcTagCallbackSupport = new ExternalNfcTagCallbackSupport(this, this, executor, receiverExported);
		externalNfcTagCallbackSupport.setEnabled(true);

		externalNfcReaderCallbackSupport = new ExternalNfcReaderCallbackSupport(this, this, executor, receiverExported);
		externalNfcReaderCallbackSupport.setEnabled(true);

		nfcReaderCallbackSupport = NfcReaderCallbackSupport.newBuilder()
				.withActivity(this)
				.withExecutor(executor)
				.withReaderCallbackDelegate(this)
				.withPresenceCheckDelay(250)
				.withReceiverExported(receiverExported)
				.build();
		nfcReaderCallbackSupport.setNfcReaderMode(true);

		phoneStatus.setText(R.string.ultralight_test_phone_ready);
		externalStatus.setText(R.string.ultralight_test_external_ready);
		updateReaders();
		render();
	}

	@Override
	protected void onResume() {
		super.onResume();

		externalNfcTagCallbackSupport.onResume();
		// also makes the external services announce their open readers
		externalNfcReaderCallbackSupport.onResume();
		nfcReaderCallbackSupport.onResume();

		IntentFilter filter = new IntentFilter(ExternalNfcTagCallback.ACTION_TECH_DISCOVERED);
		ContextCompat.registerReceiver(this, techDiscoveredReceiver, filter, ContextCompat.RECEIVER_EXPORTED);

		render();
	}

	@Override
	protected void onPause() {
		super.onPause();

		unregisterReceiver(techDiscoveredReceiver);

		externalNfcTagCallbackSupport.onPause();
		externalNfcReaderCallbackSupport.onPause();
		nfcReaderCallbackSupport.onPause();
	}

	/**
	 * Tag on the phone (built-in NFC reader).
	 */

	@Override
	public void onTagDiscovered(Tag tag, Intent intent) {
		if (ignoreIfNotUltralight(tag, "Phone", phoneStatus)) {
			return;
		}
		String uid = UltralightResults.key(tag.getId());
		UltralightTagTester.Session session = paused.remove(uid);
		if (session != null && System.currentTimeMillis() - session.getPausedAt() < RESUME_MILLIS) {
			// the same tag again, after the phone dropped it
			scan(session.getResult(), phoneStatus, (progress) -> {
				tester.resume(session, tag, progress);
				return session;
			});
			return;
		}

		ScanResult result = new ScanResult();
		result.source = ScanResult.Source.PHONE;
		result.readerKey = PHONE_KEY;
		result.readerLabel = "Phone";
		result.capability = ReaderCapability.PHONE;
		result.uid = tag.getId();

		scan(result, phoneStatus, (progress) -> tester.start(tag, intent, result, results.getReferenceFamily(tag.getId()), progress));
	}

	/**
	 * Tag on an external reader.
	 */

	@Override
	public void onExternalTagDiscovered(Tag tag, Intent intent) {
		if (tag == null) {
			return;
		}
		ScanResult result = new ScanResult();
		result.source = ScanResult.Source.EXTERNAL;
		result.uid = tag.getId();
		resolveReader(intent, result);

		if (ignoreIfNotUltralight(tag, result.readerLabel, externalStatus)) {
			return;
		}

		scan(result, externalStatus, (progress) -> tester.start(tag, intent, result, results.getReferenceFamily(tag.getId()), progress));
	}

	/**
	 * Ignore other tags, i.e. IsoDep cards.
	 */

	private boolean ignoreIfNotUltralight(Tag tag, String name, TextView status) {
		if (MifareUltralight.get(tag) != null) {
			return false;
		}
		String uid = UltralightResults.key(tag.getId());
		results.addEvent(timeFormat.format(new Date()) + " " + name + ": ignored " + uid + ", not a MIFARE Ultralight / NTAG tag");
		setStatus(status, getString(R.string.ultralight_test_ignored, name, uid));
		runOnUiThread(this::render);
		return true;
	}

	private interface Scan {
		UltralightTagTester.Session run(UltralightTagTester.Progress progress);
	}

	private void scan(ScanResult result, TextView status, Scan scan) {
		String uid = UltralightResults.key(result.uid);
		String name = result.source == ScanResult.Source.PHONE ? "Phone" : result.readerLabel;

		// the tag might be discovered again quickly after the phone dropped it, so wait for the previous scan to complete
		long deadline = System.currentTimeMillis() + 2000;
		while (busy.putIfAbsent(result.readerKey, Boolean.TRUE) != null) {
			if (System.currentTimeMillis() > deadline) {
				LOGGER.info("Ignore " + uid + " on " + name + ", already scanning");
				return;
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
		}
		try {
			// an external reader might report the tag again after a command fails
			Long last = lastScan.get(result.readerKey + uid);
			if (last != null && System.currentTimeMillis() - last < IGNORE_REPEAT_MILLIS && result.memory.length == 0) {
				LOGGER.info("Ignore repeated " + uid + " on " + name);
				return;
			}

			setStatus(status, getString(R.string.ultralight_test_reading, name, uid, ""));

			UltralightTagTester.Session session = scan.run((message) -> setStatus(status, getString(R.string.ultralight_test_reading, name, uid, message)));

			TagRecord record = results.add(result);

			if (session.isAwaitingRediscovery()) {
				paused.put(uid, session);
				setStatus(status, getString(R.string.ultralight_test_dropped, name, uid));
			} else {
				lastScan.put(result.readerKey + uid, System.currentTimeMillis());

				String outcome;
				if (result.lost) {
					outcome = getString(R.string.ultralight_test_lost);
				} else {
					int unexpected = ReportRenderer.unexpected(record, result);
					outcome = unexpected == 0 ? getString(R.string.ultralight_test_as_expected) : getString(R.string.ultralight_test_unexpected, unexpected);
				}
				setStatus(status, getString(R.string.ultralight_test_done, name, uid, outcome));

				log(record);
			}
		} catch (Exception e) {
			LOGGER.warn("Problem scanning tag", e);
			setStatus(status, name + ": " + e);
		} finally {
			busy.remove(result.readerKey);
		}
		runOnUiThread(this::render);
	}

	private void resolveReader(Intent intent, ScanResult result) {
		String readerId = intent != null ? intent.getStringExtra(ExternalNfcReaderCallback.EXTRAS_READER_ID) : null;

		if (intent != null && intent.hasExtra(Atr210NfcTagCallback.EXTRA_CLIENT_ID)) {
			result.capability = ReaderCapability.HID_ATR210;
			result.readerKey = "atr210:" + readerId;
			result.readerName = "HID ATR210 " + readerId;
			result.readerLabel = results.label(result.readerKey, "ATR210");
		} else if (readerId != null) {
			String name = results.getReaderName(readerId);
			if (readerId.startsWith("acs")) {
				result.capability = ReaderCapability.fromAcsReaderName(name);
			} else {
				result.capability = ReaderCapability.UNKNOWN;
			}
			result.readerKey = readerId;
			result.readerName = name != null ? name : readerId;
			// only keep the label once the reader name is known
			result.readerLabel = name != null ? results.label(result.readerKey, ReportRenderer.shortReaderName(name)) : readerId;
		} else {
			// the WebSocket service does not add a reader id
			result.capability = ReaderCapability.WEBSOCKET;
			result.readerKey = "websocket";
			result.readerName = "WebSocket";
			result.readerLabel = results.label(result.readerKey, "WebSocket");
		}
	}

	private String describeReader(Intent intent) {
		String readerId = intent.getStringExtra(ExternalNfcReaderCallback.EXTRAS_READER_ID);
		if (readerId == null) {
			return "external reader";
		}
		String name = results.getReaderName(readerId);
		return name != null ? name : readerId;
	}

	@Override
	public void onExternalNfcReaderOpened(Intent intent) {
		if (intent.hasExtra(ExternalNfcReaderCallback.EXTRA_READER_CONTROL)) {
			Parcelable reader = ParcelableExtraUtils.getParcelableExtra(intent, ExternalNfcReaderCallback.EXTRA_READER_CONTROL, Parcelable.class);
			if (reader instanceof AcrReader) {
				AcrReader acrReader = (AcrReader) reader;
				results.readerOpened(acrReader.getId(), acrReader.getName());
			} else if (reader instanceof ExternalNfcReader) {
				ExternalNfcReader externalNfcReader = (ExternalNfcReader) reader;
				results.readerOpened(externalNfcReader.getId(), reader.getClass().getSimpleName() + " " + externalNfcReader.getId());
			}
		}
		runOnUiThread(this::updateReaders);
	}

	@Override
	public void onExternalNfcReaderClosed(Intent intent) {
		String readerId = intent.getStringExtra(ExternalNfcReaderCallback.EXTRAS_READER_ID);
		if (readerId != null) {
			results.readerClosed(readerId);
		}
		runOnUiThread(this::updateReaders);
	}

	private void updateReaders() {
		Map<String, String> open = results.getOpenReaders();
		if (open.isEmpty()) {
			readersStatus.setText(R.string.ultralight_test_no_readers);
			return;
		}
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<String, String> entry : open.entrySet()) {
			if (builder.length() > 0) {
				builder.append('\n');
			}
			String name = entry.getValue();
			builder.append(name);
			if (entry.getKey().startsWith("acs")) {
				builder.append(" (").append(ReaderCapability.fromAcsReaderName(name).getLabel()).append(")");
			}
		}
		readersStatus.setText(getString(R.string.ultralight_test_readers, builder.toString()));
	}

	private void setStatus(TextView status, String text) {
		runOnUiThread(() -> status.setText(text));
	}

	private String header() {
		return "Ultralight / NTAG test, " + Build.MANUFACTURER + " " + Build.MODEL + ", Android " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + "), "
				+ new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ROOT).format(new Date());
	}

	private String renderReport() {
		return renderer.render(header(), results.getRecords(), results.getReaders(), results.getEvents());
	}

	private void render() {
		List<TagRecord> records = results.getRecords();
		renderTypes(TypeStatus.compute(records));
		report.setText(renderer.renderTags(records, results.getReaders(), results.getEvents()));
	}

	/**
	 * One row per tag type, with three lights: phone, external readers, and whether they correspond.
	 */

	private void renderTypes(List<TypeStatus> statuses) {
		types.removeAllViews();
		types.addView(typeRow(getString(R.string.ultralight_test_type), null, null, null, null, true));
		for (TypeStatus status : statuses) {
			types.addView(typeRow(status.getFamily().getLabel(), typeNote(status), status.getPhone(), status.getExternal(), status.getMatch(), false));
		}
	}

	private String typeNote(TypeStatus status) {
		if (status.getTags() == 0) {
			return getString(R.string.ultralight_test_not_tested);
		}
		StringBuilder builder = new StringBuilder();
		builder.append(getResources().getQuantityString(R.plurals.ultralight_test_tags, status.getTags(), status.getTags()));
		if (!status.getReaders().isEmpty()) {
			builder.append(" · ").append(ReportRenderer.join(status.getReaders()));
		}
		List<String> problems = status.getProblems();
		if (!problems.isEmpty()) {
			builder.append('\n').append(problems.get(0));
			if (problems.size() > 1) {
				builder.append(" (+").append(problems.size() - 1).append(')');
			}
		}
		return builder.toString();
	}

	private LinearLayout typeRow(String title, String note, TypeStatus.Light phone, TypeStatus.Light external, TypeStatus.Light match, boolean header) {
		LinearLayout row = new LinearLayout(this);
		row.setOrientation(LinearLayout.HORIZONTAL);
		row.setGravity(Gravity.CENTER_VERTICAL);
		row.setPadding(0, dp(3), 0, dp(3));

		LinearLayout labels = new LinearLayout(this);
		labels.setOrientation(LinearLayout.VERTICAL);
		row.addView(labels, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

		TextView titleView = new TextView(this);
		titleView.setText(title);
		titleView.setTextColor(Color.BLACK);
		titleView.setTypeface(Typeface.DEFAULT_BOLD);
		titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, header ? 13 : 15);
		labels.addView(titleView);

		if (note != null) {
			TextView noteView = new TextView(this);
			noteView.setText(note);
			noteView.setTextColor(Color.DKGRAY);
			noteView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
			labels.addView(noteView);
		}

		if (header) {
			row.addView(column(getString(R.string.ultralight_test_phone)));
			row.addView(column(getString(R.string.ultralight_test_external)));
			row.addView(column(getString(R.string.ultralight_test_match)));
		} else {
			row.addView(light(phone));
			row.addView(light(external));
			row.addView(light(match));
		}
		return row;
	}

	private TextView column(String text) {
		TextView view = new TextView(this);
		view.setText(text);
		view.setTextColor(Color.BLACK);
		view.setTypeface(Typeface.DEFAULT_BOLD);
		view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
		view.setGravity(Gravity.CENTER);
		view.setLayoutParams(new LinearLayout.LayoutParams(dp(64), LinearLayout.LayoutParams.WRAP_CONTENT));
		return view;
	}

	private TextView light(TypeStatus.Light light) {
		TextView view = new TextView(this);
		view.setText("●");
		view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
		view.setGravity(Gravity.CENTER);
		view.setTextColor(color(light));
		view.setContentDescription(ReportRenderer.lightText(light));
		view.setLayoutParams(new LinearLayout.LayoutParams(dp(64), LinearLayout.LayoutParams.WRAP_CONTENT));
		return view;
	}

	private static int color(TypeStatus.Light light) {
		switch (light) {
		case PASS:
			return 0xFF2E7D32;
		case FAIL:
			return 0xFFC62828;
		case INCOMPLETE:
			return 0xFFF9A825;
		default:
			return 0xFFBDBDBD;
		}
	}

	private int dp(int value) {
		return Math.round(value * getResources().getDisplayMetrics().density);
	}

	private void copy() {
		String text = renderReport();
		ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.ultralight_test_title), text));
		log(text);
		Toast.makeText(this, R.string.ultralight_test_copied, Toast.LENGTH_SHORT).show();
	}

	private void log(TagRecord record) {
		Map<String, String> readers = results.getReaders();
		log(renderer.details(record, readers));
	}

	/**
	 * Log line by line, to stay below the logcat line length limit.
	 */

	private static void log(String text) {
		for (String line : text.split("\n")) {
			Log.i(LOG_TAG, line);
		}
	}
}
