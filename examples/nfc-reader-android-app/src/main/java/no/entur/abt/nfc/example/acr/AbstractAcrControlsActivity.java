package no.entur.abt.nfc.example.acr;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

import no.entur.abt.nfc.example.MainApplication;
import no.entur.abt.nfc.example.R;
import no.entur.abt.nfc.example.utils.ParcelableExtraUtils;
import no.entur.android.nfc.external.ExternalNfcReaderCallback;
import no.entur.android.nfc.external.ExternalNfcReaderCallbackSupport;
import no.entur.android.nfc.external.acs.reader.AcrDefaultLEDAndBuzzerBehaviour;
import no.entur.android.nfc.external.acs.reader.AcrFont;
import no.entur.android.nfc.external.acs.reader.AcrLED;
import no.entur.android.nfc.external.acs.reader.AcrReader;
import no.entur.android.nfc.wrapper.Tag;

/**
 * Try the buzzer, LED and display controls of one ACS reader model. Every call and its result is logged on screen and in logcat
 * (tag AcrControls).
 *
 * @param <T> reader type
 */

public abstract class AbstractAcrControlsActivity<T extends AcrReader> extends AppCompatActivity implements ExternalNfcReaderCallback {

	public static final String EXTRA_READER_ID = AbstractAcrControlsActivity.class.getName() + ".extra.READER_ID";

	private static final String LOG_TAG = "AcrControls";

	protected interface Getter<V> {
		V get() throws Exception;
	}

	protected interface Setter<V> {
		boolean set(V value) throws Exception;
	}

	protected interface Display {
		boolean displayText(AcrFont font, int style, int line, String text) throws Exception;

		boolean clear() throws Exception;

		boolean backlight(boolean on) throws Exception;
	}

	private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.ROOT);

	private Executor executor;
	private ExternalNfcReaderCallbackSupport externalNfcReaderCallbackSupport;

	private LinearLayout container;
	private TextView log;
	private final List<String> logLines = new ArrayList<>();

	private String readerId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_acr_controls);

		container = findViewById(R.id.acrControls);
		log = findViewById(R.id.acrControlsLog);

		readerId = getIntent().getStringExtra(EXTRA_READER_ID);

		executor = ((MainApplication) getApplication()).getThreadPoolExecutor();

		externalNfcReaderCallbackSupport = new ExternalNfcReaderCallbackSupport(this, this, executor, true);
		externalNfcReaderCallbackSupport.setEnabled(true);

		render();
	}

	@Override
	protected void onResume() {
		super.onResume();

		// also makes the reader service announce the open reader
		externalNfcReaderCallbackSupport.onResume();

		render();
	}

	@Override
	protected void onPause() {
		super.onPause();

		externalNfcReaderCallbackSupport.onPause();
	}

	@Override
	public void onExternalNfcReaderOpened(Intent intent) {
		if (intent.hasExtra(ExternalNfcReaderCallback.EXTRA_READER_CONTROL)) {
			Parcelable reader = ParcelableExtraUtils.getParcelableExtra(intent, ExternalNfcReaderCallback.EXTRA_READER_CONTROL, Parcelable.class);
			if (reader instanceof AcrReader) {
				AcrReaders.opened((AcrReader) reader);

				runOnUiThread(this::render);
			}
		}
	}

	@Override
	public void onExternalNfcReaderClosed(Intent intent) {
		String closed = intent.getStringExtra(ExternalNfcReaderCallback.EXTRAS_READER_ID);
		if (closed != null) {
			AcrReaders.closed(closed);
		}
		runOnUiThread(this::render);
	}

	@Override
	public void onTagDiscovered(Tag tag, Intent intent) {
		// tags are not handled here
	}

	protected abstract Class<T> getReaderType();

	/**
	 * Add the controls for the reader.
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

	private void render() {
		container.removeAllViews();

		T reader = getReader();
		if (reader == null) {
			container.addView(text(getString(R.string.acr_controls_no_reader, getReaderType().getSimpleName()), false));
			return;
		}
		setTitle(reader.getName());

		addControls(reader);
	}

	// sections

	protected void addDefaultBehaviourSection(List<AcrDefaultLEDAndBuzzerBehaviour> supported, Getter<List<AcrDefaultLEDAndBuzzerBehaviour>> getter,
			Setter<AcrDefaultLEDAndBuzzerBehaviour[]> setter) {
		container.addView(section(getString(R.string.acr_controls_default_behaviour)));

		Map<AcrDefaultLEDAndBuzzerBehaviour, CheckBox> checkBoxes = new EnumMap<>(AcrDefaultLEDAndBuzzerBehaviour.class);
		for (AcrDefaultLEDAndBuzzerBehaviour behaviour : supported) {
			CheckBox checkBox = checkBox(behaviour.name());
			checkBoxes.put(behaviour, checkBox);
			container.addView(checkBox);
		}
		container.addView(row(button(getString(R.string.acr_controls_read), (v) -> run("getDefaultLEDAndBuzzerBehaviour()", () -> {
			List<AcrDefaultLEDAndBuzzerBehaviour> behaviours = getter.get();
			runOnUiThread(() -> {
				for (Map.Entry<AcrDefaultLEDAndBuzzerBehaviour, CheckBox> entry : checkBoxes.entrySet()) {
					entry.getValue().setChecked(behaviours.contains(entry.getKey()));
				}
			});
			return behaviours;
		})), button(getString(R.string.acr_controls_apply), (v) -> {
			AcrDefaultLEDAndBuzzerBehaviour[] types = checked(checkBoxes).toArray(new AcrDefaultLEDAndBuzzerBehaviour[0]);
			run("setDefaultLEDAndBuzzerBehaviour(" + Arrays.toString(types) + ")", () -> setter.set(types));
		})));
	}

	/**
	 * @param title section title
	 * @param getter getter, or null if the LEDs cannot be read
	 * @param setter setter
	 */

	protected void addLEDSection(String title, Getter<List<AcrLED>> getter, Setter<AcrLED[]> setter) {
		container.addView(section(title));

		Map<AcrLED, CheckBox> checkBoxes = new EnumMap<>(AcrLED.class);
		LinearLayout row = new LinearLayout(this);
		for (AcrLED led : AcrLED.values()) {
			CheckBox checkBox = checkBox(led.name());
			checkBoxes.put(led, checkBox);
			row.addView(checkBox);
		}
		container.addView(row);

		List<View> buttons = new ArrayList<>();
		if (getter != null) {
			buttons.add(button(getString(R.string.acr_controls_read), (v) -> run("getLEDs()", () -> {
				List<AcrLED> leds = getter.get();
				runOnUiThread(() -> {
					for (Map.Entry<AcrLED, CheckBox> entry : checkBoxes.entrySet()) {
						entry.getValue().setChecked(leds.contains(entry.getKey()));
					}
				});
				return leds;
			})));
		}
		buttons.add(button(getString(R.string.acr_controls_apply), (v) -> {
			AcrLED[] leds = checked(checkBoxes).toArray(new AcrLED[0]);
			run("setLEDs(" + Arrays.toString(leds) + ")", () -> setter.set(leds));
		}));
		buttons.add(button(getString(R.string.acr_controls_off), (v) -> run("setLEDs()", () -> setter.set(new AcrLED[0]))));
		container.addView(row(buttons.toArray(new View[0])));
	}

	/**
	 * Status LEDs: ready, progress, complete, error.
	 */

	protected void addStatusLEDSection(Setter<boolean[]> setter) {
		container.addView(section(getString(R.string.acr_controls_light_led)));
		CheckBox ready = checkBox("ready");
		CheckBox progress = checkBox("progress");
		CheckBox complete = checkBox("complete");
		CheckBox error = checkBox("error");
		container.addView(row(ready, progress, complete, error));
		container.addView(row(button(getString(R.string.acr_controls_apply), (v) -> {
			boolean[] values = { ready.isChecked(), progress.isChecked(), complete.isChecked(), error.isChecked() };
			run("lightLED(" + Arrays.toString(values) + ")", () -> setter.set(values));
		})));
	}

	/**
	 * @param display display
	 * @param contrast contrast setter, or null
	 */

	protected void addDisplaySection(Display display, Setter<Integer> contrast) {
		container.addView(section(getString(R.string.acr_controls_display)));

		RadioGroup fonts = new RadioGroup(this);
		fonts.setOrientation(RadioGroup.HORIZONTAL);
		for (AcrFont font : AcrFont.values()) {
			RadioButton button = new RadioButton(this);
			button.setId(View.generateViewId());
			button.setText(font.name());
			button.setTag(font);
			button.setChecked(font == AcrFont.FONT_C);
			fonts.addView(button);
		}
		CheckBox bold = checkBox("bold");
		container.addView(row(fonts, bold));

		EditText line = editText("0", InputType.TYPE_CLASS_NUMBER);
		EditText message = editText("Hello ÆØÅ æøå", InputType.TYPE_CLASS_TEXT);
		container.addView(row(text(getString(R.string.acr_controls_line), false), line));
		container.addView(message);

		container.addView(row(button(getString(R.string.acr_controls_show), (v) -> {
			AcrFont font = (AcrFont) findViewById(fonts.getCheckedRadioButtonId()).getTag();
			int style = bold.isChecked() ? Typeface.BOLD : Typeface.NORMAL;
			int lineNumber = parseInt(line, 0);
			String text = message.getText().toString();
			run("displayText(" + font + ", " + (bold.isChecked() ? "bold" : "normal") + ", line " + lineNumber + ", \"" + text + "\")",
					() -> display.displayText(font, style, lineNumber, text));
		}), button(getString(R.string.acr_controls_clear), (v) -> run("clearDisplay()", display::clear))));

		container.addView(row(button(getString(R.string.acr_controls_backlight_on), (v) -> run("lightDisplayBacklight(true)", () -> display.backlight(true))),
				button(getString(R.string.acr_controls_backlight_off), (v) -> run("lightDisplayBacklight(false)", () -> display.backlight(false)))));

		if (contrast != null) {
			EditText value = editText("8", InputType.TYPE_CLASS_NUMBER);
			container.addView(row(text(getString(R.string.acr_controls_contrast), false), value, button(getString(R.string.acr_controls_apply), (v) -> {
				int level = parseInt(value, 8);
				run("setDisplayContrast(" + level + ")", () -> contrast.set(level));
			})));
		}
	}

	protected void addSection(String title) {
		container.addView(section(title));
	}

	protected void addRow(View... views) {
		container.addView(row(views));
	}

	// helpers

	protected void run(String description, Callable<Object> call) {
		log(description + " ...");
		executor.execute(() -> {
			try {
				Object result = call.call();
				log(description + " -> " + result);
			} catch (Exception e) {
				Log.w(LOG_TAG, description, e);
				log(description + " failed: " + e);
			}
		});
	}

	private void log(String line) {
		Log.i(LOG_TAG, line);
		String text = timeFormat.format(new Date()) + " " + line;
		runOnUiThread(() -> {
			logLines.add(0, text);
			while (logLines.size() > 50) {
				logLines.remove(logLines.size() - 1);
			}
			StringBuilder builder = new StringBuilder();
			for (String l : logLines) {
				builder.append(l).append('\n');
			}
			log.setText(builder);
		});
	}

	private static <E extends Enum<E>> List<E> checked(Map<E, CheckBox> checkBoxes) {
		List<E> checked = new ArrayList<>();
		for (Map.Entry<E, CheckBox> entry : checkBoxes.entrySet()) {
			if (entry.getValue().isChecked()) {
				checked.add(entry.getKey());
			}
		}
		return checked;
	}

	protected static int parseInt(EditText editText, int defaultValue) {
		try {
			return Integer.parseInt(editText.getText().toString().trim());
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	private TextView section(String title) {
		TextView view = text(title, true);
		view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		view.setPadding(0, dp(16), 0, dp(4));
		return view;
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

	protected CheckBox checkBox(String label) {
		CheckBox checkBox = new CheckBox(this);
		checkBox.setText(label);
		return checkBox;
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

	private LinearLayout row(View... views) {
		LinearLayout row = new LinearLayout(this);
		row.setOrientation(LinearLayout.HORIZONTAL);
		for (View view : views) {
			row.addView(view);
		}
		return row;
	}

	private int dp(int value) {
		return Math.round(value * getResources().getDisplayMetrics().density);
	}
}
