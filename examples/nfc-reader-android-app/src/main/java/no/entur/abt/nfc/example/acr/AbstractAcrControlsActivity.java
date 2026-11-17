package no.entur.abt.nfc.example.acr;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import no.entur.abt.nfc.example.MainApplication;
import no.entur.abt.nfc.example.R;
import no.entur.abt.nfc.example.utils.ParcelableExtraUtils;
import no.entur.android.nfc.external.ExternalNfcReaderCallback;
import no.entur.android.nfc.external.ExternalNfcReaderCallbackSupport;
import no.entur.android.nfc.external.acs.reader.AcrAutomaticPICCPolling;
import no.entur.android.nfc.external.acs.reader.AcrCommunicationSpeed;
import no.entur.android.nfc.external.acs.reader.AcrFont;
import no.entur.android.nfc.external.acs.reader.AcrReader;
import no.entur.android.nfc.wrapper.Tag;

/**
 * Settings and controls of one ACS reader model, in the style of the old per-reader apps: a card per section, with a switch per
 * setting.<br>
 * <br>
 * Settings are applied as soon as they are changed. Settings which can be read from the reader are disabled until they have been
 * read, and are read again after every change, so the screen shows what the reader actually has. Reader calls run one at a time,
 * in order. Every call and its result is logged to logcat (tag AcrControls).
 *
 * @param <T> reader type
 */

public abstract class AbstractAcrControlsActivity<T extends AcrReader> extends AppCompatActivity implements ExternalNfcReaderCallback {

	public static final String EXTRA_READER_ID = AbstractAcrControlsActivity.class.getName() + ".extra.READER_ID";

	private static final String LOG_TAG = "AcrControls";

	// com.acs.smartcard.Reader.IOCTL_CCID_ESCAPE
	private static final int IOCTL_CCID_ESCAPE = 3500;

	protected static final List<AcrAutomaticPICCPolling> POLLING_INTERVALS = Arrays.asList(AcrAutomaticPICCPolling.PICC_POLLING_INTERVAL_250,
			AcrAutomaticPICCPolling.PICC_POLLING_INTERVAL_500, AcrAutomaticPICCPolling.PICC_POLLING_INTERVAL_1000, AcrAutomaticPICCPolling.PICC_POLLING_INTERVAL_2500);

	protected interface Getter<V> {
		V get() throws Exception;
	}

	protected interface Setter<V> {
		boolean set(V value) throws Exception;
	}

	protected interface PairSetter<V> {
		boolean set(V first, V second) throws Exception;
	}

	protected interface Labeler<V> {
		String label(V value);
	}

	protected interface Consumer<V> {
		void accept(V value);
	}

	protected interface Display {
		boolean displayText(AcrFont font, int style, int line, String text) throws Exception;

		boolean clear() throws Exception;

		boolean backlight(boolean on) throws Exception;
	}

	// reader calls one at a time, in order
	private final ExecutorService readerExecutor = Executors.newSingleThreadExecutor();

	private ExternalNfcReaderCallbackSupport externalNfcReaderCallbackSupport;

	private LinearLayout container;
	private LinearLayout card;

	// true while the controls are updated with values read from the reader
	private boolean updating;

	private String readerId;
	private String renderedReaderId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_acr_controls);

		container = findViewById(R.id.acrControls);

		readerId = getIntent().getStringExtra(EXTRA_READER_ID);

		externalNfcReaderCallbackSupport = new ExternalNfcReaderCallbackSupport(this, this, ((MainApplication) getApplication()).getThreadPoolExecutor(), true);
		externalNfcReaderCallbackSupport.setEnabled(true);

		render();
	}

	@Override
	protected void onResume() {
		super.onResume();

		// also makes the reader service announce the open reader
		externalNfcReaderCallbackSupport.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();

		externalNfcReaderCallbackSupport.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		readerExecutor.shutdown();
	}

	@Override
	public void onExternalNfcReaderOpened(Intent intent) {
		if (intent.hasExtra(ExternalNfcReaderCallback.EXTRA_READER_CONTROL)) {
			Parcelable reader = ParcelableExtraUtils.getParcelableExtra(intent, ExternalNfcReaderCallback.EXTRA_READER_CONTROL, Parcelable.class);
			if (reader instanceof AcrReader) {
				AcrReaders.opened((AcrReader) reader);

				runOnUiThread(this::renderIfChanged);
			}
		}
	}

	@Override
	public void onExternalNfcReaderClosed(Intent intent) {
		String closed = intent.getStringExtra(ExternalNfcReaderCallback.EXTRAS_READER_ID);
		if (closed != null) {
			AcrReaders.closed(closed);
		}
		runOnUiThread(this::renderIfChanged);
	}

	@Override
	public void onTagDiscovered(Tag tag, Intent intent) {
		// tags are not handled here
	}

	protected abstract Class<T> getReaderType();

	/**
	 * Add reader details (rows in the reader section), in addition to name, id, firmware and slots.
	 *
	 * @param reader reader
	 */

	protected void addReaderDetails(T reader) {
	}

	/**
	 * Add the sections for the reader.
	 *
	 * @param reader reader
	 */

	protected abstract void addControls(T reader);

	/**
	 * @return the reader given in the intent, or the first open reader of this type
	 */

	private T getReader() {
		Class<T> type = getReaderType();
		for (AcrReader reader : AcrReaders.getReaders()) {
			if (type.isInstance(reader) && (readerId == null || readerId.equals(reader.getId()))) {
				return type.cast(reader);
			}
		}
		return null;
	}

	private void renderIfChanged() {
		T reader = getReader();
		String id = reader != null ? reader.getId() : null;
		if (id == null ? renderedReaderId != null : !id.equals(renderedReaderId)) {
			render();
		}
	}

	private void render() {
		container.removeAllViews();

		T reader = getReader();
		renderedReaderId = reader != null ? reader.getId() : null;

		addSection(getString(R.string.acr_controls_reader));
		if (reader == null) {
			addNote(getString(R.string.acr_controls_no_reader, getReaderType().getSimpleName()));
			return;
		}
		setTitle(reader.getName());

		addValueRow(getString(R.string.acr_controls_name), reader.getName());
		addValueRow(getString(R.string.acr_controls_id), reader.getId());
		addValue(getString(R.string.acr_controls_firmware), reader::getFirmware);
		addValue(getString(R.string.acr_controls_slots), reader::getNumberOfSlots);
		addReaderDetails(reader);

		addControls(reader);

		addEscapeCommand(reader);
	}

	/**
	 * @return escape commands to offer as shortcuts, as "label: hex"
	 */

	protected List<String> getEscapeCommandPresets() {
		return Collections.emptyList();
	}

	/**
	 * Send an escape command (hex) to the reader, and log the response.
	 */

	private void addEscapeCommand(T reader) {
		addSection(getString(R.string.acr_controls_escape));

		EditText command = editText("", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
		command.setHint("E0 00 00 18 00");
		command.setTypeface(Typeface.MONOSPACE);
		card.addView(command, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

		addRow(button(getString(R.string.acr_controls_send), (v) -> sendEscapeCommand(reader, command.getText().toString())));

		for (String preset : getEscapeCommandPresets()) {
			int separator = preset.indexOf(':');
			String label = separator == -1 ? null : preset.substring(0, separator).trim();
			String hex = preset.substring(separator + 1).trim();
			Button button = button(label == null ? hex : label + "\n" + hex, (v) -> {
				command.setText(hex);
				sendEscapeCommand(reader, hex);
			});
			button.setTypeface(Typeface.MONOSPACE);
			button.setAllCaps(false);
			card.addView(button);
		}
	}

	private void sendEscapeCommand(T reader, String hex) {
		byte[] command;
		try {
			command = fromHex(hex);
		} catch (IllegalArgumentException e) {
			log("escape " + hex + " failed: " + e.getMessage());
			return;
		}
		run("escape " + toHex(command), () -> toHex(reader.control(0, IOCTL_CCID_ESCAPE, command)));
	}

	private static byte[] fromHex(String hex) {
		String digits = hex.replaceAll("\\s", "");
		if (digits.isEmpty() || digits.length() % 2 != 0) {
			throw new IllegalArgumentException("expected an even number of hex digits");
		}
		byte[] bytes = new byte[digits.length() / 2];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) Integer.parseInt(digits.substring(i * 2, i * 2 + 2), 16);
		}
		return bytes;
	}

	private static String toHex(byte[] bytes) {
		if (bytes == null) {
			return "null";
		}
		StringBuilder builder = new StringBuilder();
		for (byte b : bytes) {
			if (builder.length() > 0) {
				builder.append(' ');
			}
			builder.append(String.format(Locale.ROOT, "%02X", b & 0xFF));
		}
		return builder.toString();
	}

	// sections and settings

	/**
	 * Start a new section (card).
	 */

	protected void addSection(String title) {
		card = new LinearLayout(this);
		card.setOrientation(LinearLayout.VERTICAL);
		card.setPadding(dp(12), dp(8), dp(12), dp(8));

		GradientDrawable background = new GradientDrawable();
		background.setColor(0xDDFFFFFF);
		background.setCornerRadius(dp(8));
		card.setBackground(background);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		params.setMargins(0, dp(6), 0, dp(6));
		container.addView(card, params);

		TextView view = text(title, true);
		view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		view.setPadding(0, 0, 0, dp(4));
		card.addView(view);
	}

	/**
	 * A sub heading within the section.
	 */

	protected void addSubheading(String title) {
		TextView view = text(title, true);
		view.setPadding(0, dp(8), 0, 0);
		card.addView(view);
	}

	/**
	 * A small grey note.
	 */

	protected TextView addNote(String value) {
		TextView view = note(value);
		card.addView(view);
		return view;
	}

	/**
	 * A switch per flag, plus an optional choice of exactly one of the given choices (i.e. an interval). The flags and the choice
	 * are written together when one of them is changed.
	 *
	 * @param type enum type
	 * @param flags flags
	 * @param choicesTitle title of the choices, or null
	 * @param choices choices (one of), or empty
	 * @param labeler labels, or null for the default labels
	 * @param getter getter, or null if the value cannot be read from the reader
	 * @param setter setter
	 */

	protected <E extends Enum<E>> void addFlags(Class<E> type, List<E> flags, String choicesTitle, List<E> choices, Labeler<E> labeler, Getter<List<E>> getter,
			Setter<E[]> setter) {
		Labeler<E> labels = labeler != null ? labeler : AcrLabels::label;
		String title = currentTitle();

		Map<E, CompoundButton> buttons = new LinkedHashMap<>();
		List<View> controls = new ArrayList<>();
		Runnable[] reload = new Runnable[1];

		Runnable apply = () -> {
			List<E> values = new ArrayList<>();
			for (Map.Entry<E, CompoundButton> entry : buttons.entrySet()) {
				if (entry.getValue().isChecked()) {
					values.add(entry.getKey());
				}
			}
			@SuppressWarnings("unchecked")
			E[] array = values.toArray((E[]) Array.newInstance(type, values.size()));
			run(title + " = " + values, () -> setter.set(array));
			if (reload[0] != null) {
				reload[0].run();
			}
		};

		for (E flag : flags) {
			SwitchCompat view = addSwitchRow(labels.label(flag), apply);
			buttons.put(flag, view);
			controls.add(view);
		}
		if (!choices.isEmpty()) {
			if (choicesTitle != null) {
				addSubheading(choicesTitle);
			}
			RadioGroup group = addRadioGroup(choices, labels, apply);
			for (int i = 0; i < group.getChildCount(); i++) {
				RadioButton button = (RadioButton) group.getChildAt(i);
				@SuppressWarnings("unchecked")
				E choice = (E) button.getTag();
				buttons.put(choice, button);
				controls.add(button);
			}
		}

		if (getter != null) {
			reload[0] = load("read " + title, getter, (values) -> {
				for (Map.Entry<E, CompoundButton> entry : buttons.entrySet()) {
					entry.getValue().setChecked(values.contains(entry.getKey()));
				}
			}, controls);
		} else {
			addNote(getString(R.string.acr_controls_not_readable));
		}
	}

	/**
	 * A switch per flag, written together when one of them is changed.
	 */

	protected <E extends Enum<E>> void addFlags(Class<E> type, List<E> flags, Labeler<E> labeler, Getter<List<E>> getter, Setter<E[]> setter) {
		addFlags(type, flags, null, Collections.<E>emptyList(), labeler, getter, setter);
	}

	/**
	 * A switch per bit of a byte setting, written together when one of them is changed.
	 *
	 * @param labels label per bit, from bit 0
	 * @param getter getter
	 * @param setter setter
	 */

	protected void addBits(String[] labels, Getter<Integer> getter, Setter<Integer> setter) {
		String title = currentTitle();
		SwitchCompat[] switches = new SwitchCompat[labels.length];
		Runnable[] reload = new Runnable[1];
		Runnable apply = () -> {
			int value = 0;
			for (int i = 0; i < switches.length; i++) {
				if (switches[i].isChecked()) {
					value |= 1 << i;
				}
			}
			int finalValue = value;
			run(title + " = " + String.format(Locale.ROOT, "%02X", value), () -> setter.set(finalValue));
			reload[0].run();
		};
		for (int i = 0; i < labels.length; i++) {
			switches[i] = addSwitchRow(labels[i], apply);
		}
		reload[0] = load("read " + title, () -> {
			int value = getter.get();
			return String.format(Locale.ROOT, "%02X", value);
		}, (hex) -> {
			int value = Integer.parseInt(hex, 16);
			for (int i = 0; i < switches.length; i++) {
				switches[i].setChecked((value & (1 << i)) != 0);
			}
		}, Arrays.<View>asList(switches));
	}

	/**
	 * The raw default LED and buzzer behaviour byte (E0 00 00 21), a switch per bit, for finding out what the bits do.
	 *
	 * @param labels label per bit, from bit 0
	 */

	protected void addDefaultBehaviourBits(T reader, String[] labels) {
		addSection(getString(R.string.acr_controls_default_behaviour_bits));
		addBits(labels, () -> {
			byte[] response = escape(reader, "E0 00 00 21 00");
			return response[5] & 0xFF;
		}, (value) -> {
			byte[] response = escape(reader, String.format(Locale.ROOT, "E0 00 00 21 01 %02X", value));
			return (response[5] & 0xFF) == value;
		});
	}

	/**
	 * Send an escape command to the reader.
	 *
	 * @return the response
	 */

	protected byte[] escape(T reader, String hex) {
		return reader.control(0, IOCTL_CCID_ESCAPE, fromHex(hex));
	}

	/**
	 * A setting with a switch.
	 *
	 * @param label label
	 * @param getter getter, or null if the value cannot be read from the reader
	 * @param setter setter
	 */

	protected void addSwitch(String label, Getter<Boolean> getter, Setter<Boolean> setter) {
		SwitchCompat[] view = new SwitchCompat[1];
		Runnable[] reload = new Runnable[1];
		view[0] = addSwitchRow(label, () -> {
			boolean value = view[0].isChecked();
			run(label + " = " + value, () -> setter.set(value));
			if (reload[0] != null) {
				reload[0].run();
			}
		});
		if (getter != null) {
			reload[0] = load("read " + label, getter, view[0]::setChecked, Collections.singletonList(view[0]));
		} else {
			addNote(getString(R.string.acr_controls_not_readable));
		}
	}

	/**
	 * A setting with one of several values.
	 *
	 * @param label label
	 * @param values values
	 * @param labeler labels, or null for the default labels
	 * @param getter getter, or null if the value cannot be read from the reader
	 * @param setter setter
	 */

	protected <V> void addChoice(String label, List<V> values, Labeler<V> labeler, Getter<V> getter, Setter<V> setter) {
		Labeler<V> labels = labeler != null ? labeler : AcrLabels::label;

		addSubheading(label);
		RadioGroup[] group = new RadioGroup[1];
		Runnable[] reload = new Runnable[1];
		group[0] = addRadioGroup(values, labels, () -> {
			V value = getSelected(group[0]);
			run(label + " = " + labels.label(value), () -> setter.set(value));
			if (reload[0] != null) {
				reload[0].run();
			}
		});
		if (getter != null) {
			reload[0] = load("read " + label, getter, (value) -> setSelected(group[0], value), getChildren(group[0]));
		} else {
			addNote(getString(R.string.acr_controls_not_readable));
		}
	}

	/**
	 * A value read from the reader.
	 */

	protected void addValue(String label, Getter<?> getter) {
		TextView value = addValueRow(label, "…");
		readerExecutor.execute(() -> {
			String text;
			try {
				Object result = getter.get();
				log("read " + label + " -> " + result);
				text = AcrLabels.label(result);
			} catch (Exception e) {
				Log.w(LOG_TAG, "read " + label, e);
				log("read " + label + " failed: " + e);
				text = "-";
			}
			String finalText = text;
			runOnUiThread(() -> value.setText(finalText));
		});
	}

	/**
	 * Automatic PPS: the maximum Tx and Rx speeds (106, 212 or 424 kbps), and the current speeds.
	 *
	 * @param getter max Tx, current Tx, max Rx, current Rx
	 * @param setter max Tx, max Rx
	 */

	protected void addAutomaticPPS(Getter<List<AcrCommunicationSpeed>> getter, PairSetter<AcrCommunicationSpeed> setter) {
		addSection(getString(R.string.acr_controls_pps));
		TextView current = addValueRow(getString(R.string.acr_controls_pps_current), "…");

		List<AcrCommunicationSpeed> speeds = Arrays.asList(AcrCommunicationSpeed.RATE_106_KBPS, AcrCommunicationSpeed.RATE_212_KBPS, AcrCommunicationSpeed.RATE_424_KBPS);
		RadioGroup[] groups = new RadioGroup[2];
		Runnable[] reload = new Runnable[1];
		Runnable apply = () -> {
			AcrCommunicationSpeed tx = getSelected(groups[0]);
			AcrCommunicationSpeed rx = getSelected(groups[1]);
			if (tx != null && rx != null) {
				run(getString(R.string.acr_controls_pps) + " = " + tx + ", " + rx, () -> setter.set(tx, rx));
				reload[0].run();
			}
		};
		addSubheading(getString(R.string.acr_controls_pps_tx));
		groups[0] = addRadioGroup(speeds, AcrLabels::label, apply);
		addSubheading(getString(R.string.acr_controls_pps_rx));
		groups[1] = addRadioGroup(speeds, AcrLabels::label, apply);

		List<View> controls = getChildren(groups[0]);
		controls.addAll(getChildren(groups[1]));

		// max tx, current tx, max rx, current rx
		reload[0] = load("read " + getString(R.string.acr_controls_pps), getter, (speed) -> {
			if (speed.size() >= 4) {
				setSelected(groups[0], speed.get(0));
				setSelected(groups[1], speed.get(2));
				current.setText(getString(R.string.acr_controls_pps_value, AcrLabels.label(speed.get(1)), AcrLabels.label(speed.get(3))));
			}
		}, controls);
	}

	/**
	 * Display text, clear, backlight and (optionally) contrast.
	 *
	 * @param display display
	 * @param contrast contrast setter, or null
	 */

	protected void addDisplay(Display display, Setter<Integer> contrast) {
		RadioGroup fonts = addRadioGroup(Arrays.asList(AcrFont.values()), AcrFont::name, null);
		// font A has all of ÆØÅæøå, font C has no Ø / ø
		setSelected(fonts, AcrFont.FONT_A);

		SwitchCompat bold = addSwitchRow(getString(R.string.acr_controls_bold), null);
		EditText line = editText("0", InputType.TYPE_CLASS_NUMBER);
		addRow(text(getString(R.string.acr_controls_line), false), line);
		EditText message = editText("Hello ÆØÅ æøå", InputType.TYPE_CLASS_TEXT);
		card.addView(message);

		addRow(button(getString(R.string.acr_controls_show), (v) -> {
			AcrFont font = getSelected(fonts);
			int style = bold.isChecked() ? Typeface.BOLD : Typeface.NORMAL;
			int lineNumber = parseInt(line, 0);
			String text = message.getText().toString();
			run("displayText(" + font + ", " + (bold.isChecked() ? "bold" : "normal") + ", line " + lineNumber + ", \"" + text + "\")",
					() -> display.displayText(font, style, lineNumber, text));
		}), button(getString(R.string.acr_controls_clear), (v) -> run("clearDisplay()", display::clear)));

		addSwitch(getString(R.string.acr_controls_backlight), null, display::backlight);

		if (contrast != null) {
			EditText value = editText("8", InputType.TYPE_CLASS_NUMBER);
			addRow(text(getString(R.string.acr_controls_contrast), false), value, button(getString(R.string.acr_controls_apply), (v) -> {
				int level = parseInt(value, 8);
				run(getString(R.string.acr_controls_contrast) + " = " + level, () -> contrast.set(level));
			}));
		}
	}

	protected void addRow(View... views) {
		LinearLayout row = new LinearLayout(this);
		row.setOrientation(LinearLayout.HORIZONTAL);
		row.setGravity(Gravity.CENTER_VERTICAL);
		for (View view : views) {
			row.addView(view);
		}
		card.addView(row);
	}

	// building blocks

	/**
	 * Read a value from the reader, and update the controls. The controls are disabled until the value has been read; if reading
	 * fails, they stay disabled (tap the note to try again).
	 *
	 * @return runnable which reads the value again
	 */

	protected <V> Runnable load(String description, Getter<V> getter, Consumer<V> onValue, List<View> controls) {
		TextView status = addNote(getString(R.string.acr_controls_reading));
		Runnable[] load = new Runnable[1];
		load[0] = () -> {
			for (View control : controls) {
				control.setEnabled(false);
			}
			status.setVisibility(View.VISIBLE);
			status.setText(R.string.acr_controls_reading);
			status.setOnClickListener(null);

			readerExecutor.execute(() -> {
				log(description + " ...");
				try {
					V value = getter.get();
					log(description + " -> " + value);
					runOnUiThread(() -> {
						updating = true;
						try {
							onValue.accept(value);
						} finally {
							updating = false;
						}
						for (View control : controls) {
							control.setEnabled(true);
						}
						status.setVisibility(View.GONE);
					});
				} catch (Exception e) {
					Log.w(LOG_TAG, description, e);
					log(description + " failed: " + e);
					runOnUiThread(() -> {
						status.setText(getString(R.string.acr_controls_read_failed, String.valueOf(e)));
						status.setOnClickListener((v) -> load[0].run());
					});
				}
			});
		};
		load[0].run();
		return load[0];
	}

	/**
	 * A row with a label and a switch.
	 *
	 * @param label label
	 * @param onToggle called when toggled by the user, or null
	 * @return the switch
	 */

	protected SwitchCompat addSwitchRow(String label, Runnable onToggle) {
		SwitchCompat view = new SwitchCompat(this);
		view.setText(label);
		view.setTextColor(Color.BLACK);
		view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		view.setMinHeight(dp(56));
		view.setPadding(0, dp(4), 0, dp(4));
		if (onToggle != null) {
			view.setOnCheckedChangeListener((button, checked) -> {
				if (!updating) {
					onToggle.run();
				}
			});
		}
		card.addView(view, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		return view;
	}

	/**
	 * A horizontal (scrollable) group of radio buttons, with the values as tags.
	 *
	 * @param onSelect called when selected by the user, or null
	 */

	protected <V> RadioGroup addRadioGroup(List<V> values, Labeler<V> labeler, Runnable onSelect) {
		RadioGroup group = new RadioGroup(this);
		group.setOrientation(RadioGroup.HORIZONTAL);
		for (V value : values) {
			RadioButton button = new RadioButton(this);
			button.setId(View.generateViewId());
			button.setText(labeler.label(value));
			button.setTextColor(Color.BLACK);
			button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
			button.setMinHeight(dp(56));
			button.setPadding(button.getPaddingLeft(), 0, dp(16), 0);
			button.setTag(value);
			group.addView(button);
		}
		if (onSelect != null) {
			group.setOnCheckedChangeListener((g, id) -> {
				if (!updating && id != View.NO_ID) {
					onSelect.run();
				}
			});
		}
		HorizontalScrollView scroll = new HorizontalScrollView(this);
		scroll.addView(group);
		card.addView(scroll);
		return group;
	}

	@SuppressWarnings("unchecked")
	protected static <V> V getSelected(RadioGroup group) {
		View view = group.findViewById(group.getCheckedRadioButtonId());
		return view != null ? (V) view.getTag() : null;
	}

	protected static void setSelected(RadioGroup group, Object value) {
		for (int i = 0; i < group.getChildCount(); i++) {
			View child = group.getChildAt(i);
			if (child.getTag() != null && child.getTag().equals(value)) {
				group.check(child.getId());
				return;
			}
		}
		group.clearCheck();
	}

	protected static List<View> getChildren(RadioGroup group) {
		List<View> children = new ArrayList<>();
		for (int i = 0; i < group.getChildCount(); i++) {
			children.add(group.getChildAt(i));
		}
		return children;
	}

	/**
	 * A row with a label and a value.
	 *
	 * @return the value view
	 */

	protected TextView addValueRow(String label, String value) {
		TextView labelView = text(label, false);
		TextView valueView = text(value, false);
		valueView.setGravity(Gravity.END);
		valueView.setTextIsSelectable(true);
		LinearLayout row = new LinearLayout(this);
		row.setPadding(0, dp(2), 0, dp(2));
		row.addView(labelView, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
		row.addView(valueView, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f));
		card.addView(row);
		return valueView;
	}

	/**
	 * Run a call on the reader (one at a time, in order), and log the result.
	 */

	protected void run(String description, Callable<Object> call) {
		log(description + " ...");
		readerExecutor.execute(() -> {
			try {
				Object result = call.call();
				log(description + " -> " + result);
			} catch (Exception e) {
				Log.w(LOG_TAG, description, e);
				log(description + " failed: " + e);
			}
		});
	}

	private static void log(String line) {
		Log.i(LOG_TAG, line);
	}

	private String currentTitle() {
		return ((TextView) card.getChildAt(0)).getText().toString();
	}

	protected static int parseInt(EditText editText, int defaultValue) {
		try {
			return Integer.parseInt(editText.getText().toString().trim());
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	protected TextView text(String value, boolean bold) {
		TextView view = new TextView(this);
		view.setText(value);
		view.setTextColor(Color.BLACK);
		if (bold) {
			view.setTypeface(Typeface.DEFAULT_BOLD);
		}
		return view;
	}

	private TextView note(String value) {
		TextView view = new TextView(this);
		view.setText(value);
		view.setTextColor(Color.DKGRAY);
		view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
		view.setPadding(0, dp(2), 0, dp(2));
		return view;
	}

	protected EditText editText(String value, int inputType) {
		EditText editText = new EditText(this);
		editText.setText(value);
		editText.setInputType(inputType);
		editText.setMinWidth(dp(64));
		return editText;
	}

	protected Button button(String label, View.OnClickListener listener) {
		Button button = new Button(this);
		button.setText(label);
		button.setOnClickListener(listener);
		return button;
	}

	private int dp(int value) {
		return Math.round(value * getResources().getDisplayMetrics().density);
	}
}
